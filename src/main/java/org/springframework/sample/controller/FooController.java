package org.springframework.sample.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class FooController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FooController.class);

  private Tracer tracer;

  @Autowired
  FooController(Tracer tracer) {
    this.tracer = tracer;
  }

  //The trace span is generated OK for this one
  @GetMapping(path = "/sync")
  Mono<String> sync() {
    return Mono.fromSupplier(() -> {
      LOGGER.info("processing");
      return "hello";
    });
  }

  @GetMapping(path = "/async")
  Mono<String> async() {
    return Mono
        .fromSupplier(() -> {
          //Note the lack of a span here if we don't change the scheduler
          LOGGER.info("processing");
          return "hello";
        })
        .subscribeOn(Schedulers.single());
  }

    @GetMapping(path = "/asyncManual")
  Mono<String> asyncManual() {
    //Should be our root span
    Span span = tracer.getCurrentSpan();
    tracer.detach(span);
    return Mono
        .fromSupplier(() -> {
          tracer.continueSpan(span);
          //Note the lack of a span here if we don't change the scheduler
          LOGGER.info("processing");
          return "hello";
        })
        .subscribeOn(Schedulers.single());
  }

}
