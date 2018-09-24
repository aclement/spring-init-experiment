package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import boot.autoconfigure.gson.GsonAutoConfigurationModule;
import boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfigurationModule;
import reactor.core.publisher.Mono;
import slim.ImportModule;


@SpringBootConfiguration
@ImportModule(value = { GsonAutoConfigurationModule.class,
		ReactiveWebServerFactoryAutoConfigurationModule.class })
public class SampleApplication {

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok().body(Mono.just("Hello"), String.class));
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}
}
