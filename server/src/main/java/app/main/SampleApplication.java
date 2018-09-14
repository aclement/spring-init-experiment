package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import boot.autoconfigure.gson.GsonAutoConfigurationModule;
import boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfigurationModule;
import reactor.core.publisher.Mono;
import slim.SlimConfiguration;

@SpringBootConfiguration
@Import({ GsonAutoConfiguration.class,
		ReactiveWebServerFactoryAutoConfiguration.class })
// Generated:
@SlimConfiguration(module = { SampleModule.class, GsonAutoConfigurationModule.class,
		ReactiveWebServerFactoryAutoConfigurationModule.class })
public class SampleApplication {

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok().body(Mono.just("Hello"), String.class));
	}

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setApplicationContextClass(ReactiveWebServerApplicationContext.class);
		app.run(args);
	}

	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return new Initializer();
	}

	private static class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			context.registerBean(SampleApplication.class);
			context.registerBean(RouterFunction.class,
					() -> context.getBean(SampleApplication.class).userEndpoints());
		}

	}
}
