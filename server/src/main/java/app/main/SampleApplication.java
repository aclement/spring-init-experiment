	package app.main;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfigurationModule;
import reactor.core.publisher.Mono;


@SpringBootConfiguration
// @EnableAutoConfiguration
// TODO [check-with-dave] had to add JacksonAutoConfiguration - is that expected pulled in via another route?
@Import(value = { ReactiveWebServerFactoryAutoConfigurationModule.class, JacksonAutoConfiguration.class })
public class SampleApplication {
	
	@Value("${app.value}")
	private String value;

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok().body(Mono.just(value), String.class));
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}
}
