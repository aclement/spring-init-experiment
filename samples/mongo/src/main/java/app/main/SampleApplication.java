package app.main;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * You need to start Mongo locally to run the app (e.g. use the docker-compose.yml
 * provided). Starts up with embedded Mongo in tests.
 * 
 * @author Dave Syer
 *
 */
@SpringBootConfiguration
@Import({ PropertyPlaceholderAutoConfiguration.class,
		ConfigurationPropertiesAutoConfiguration.class,
		EmbeddedMongoAutoConfiguration.class, MongoAutoConfiguration.class,
		MongoDataAutoConfiguration.class, MongoRepositoriesAutoConfiguration.class,
		JacksonAutoConfiguration.class, ReactiveWebServerFactoryAutoConfiguration.class,
		WebFluxAutoConfiguration.class, ErrorWebFluxAutoConfiguration.class,
		HttpHandlerAutoConfiguration.class })
public class SampleApplication {

	private CustomerRepository foos;

	public SampleApplication(CustomerRepository foos) {
		this.foos = foos;
	}

	@Bean
	public CommandLineRunner runner() {
		return args -> {
			Foo foo = foos.findAll().isEmpty() ? null : foos.findAll().iterator().next();
			if (foo == null) {
				foos.save(new Foo("Hello"));
			}
		};
	}

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"),
				request -> ok()
						.body(Mono.fromCallable(() -> foos.findAll().iterator().next())
								.subscribeOn(Schedulers.elastic()), Foo.class));
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

	@Configuration
	@EntityScan
	@AutoConfigurationPackage
	protected static class Packages {
	}

}

interface CustomerRepository extends MongoRepository<Foo, String> {
}