package org.springframework.sample.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.sleuth.SpanNamer;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.async.TraceableScheduledExecutorService;
import org.springframework.cloud.sleuth.instrument.reactor.ReactorSleuth;
import org.springframework.cloud.sleuth.instrument.reactor.TraceReactorAutoConfiguration;
import org.springframework.cloud.sleuth.instrument.web.TraceWebFluxAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;


/**
 * Copied from {@link TraceReactorAutoConfiguration.TraceReactorConfiguration} since we want to propoagate the spans..
 */
//@Configuration
//@AutoConfigureAfter(TraceWebFluxAutoConfiguration.class)
public class TraceReactorConfiguration {
  @Autowired
  Tracer tracer;
  @Autowired
  TraceKeys traceKeys;
  @Autowired
  SpanNamer spanNamer;

  @PostConstruct
  public void setupHooks() {
    Hooks.onLastOperator(ReactorSleuth.spanOperator(this.tracer));
    Schedulers.setFactory(new Schedulers.Factory() {
      @Override public ScheduledExecutorService decorateExecutorService(String schedulerType,
                                                                        Supplier<? extends ScheduledExecutorService> actual) {
        return new TraceableScheduledExecutorService(actual.get(),
            TraceReactorConfiguration.this.tracer,
            TraceReactorConfiguration.this.traceKeys,
            TraceReactorConfiguration.this.spanNamer);
      }
    });
  }

  @PreDestroy
  public void cleanupHooks() {
    Hooks.resetOnLastOperator();
    Schedulers.resetFactory();
  }
}
