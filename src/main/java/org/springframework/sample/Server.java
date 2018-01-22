package org.springframework.sample;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


import java.util.function.Function;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
@SpringBootApplication
public class Server {

  private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

  public static void main(String[] args) {
    SpringApplication.run(Server.class);
  }

  @Bean
  RouterFunction<ServerResponse> handlers() {
    return route(GET("/sync"), request -> {


      LOGGER.info("hello");
      return ServerResponse.ok().syncBody("{}");
    }).andRoute(GET("/async"), request -> {
      return Mono.fromCallable(() -> {
        LOGGER.info("moo");
        return ServerResponse.ok().syncBody("{}");
      }).subscribeOn(Schedulers.elastic()).flatMap(Function.identity());
    });
  }

}
