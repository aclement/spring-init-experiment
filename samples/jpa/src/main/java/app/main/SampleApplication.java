package app.main;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@SpringBootConfiguration
@Import({ DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class,
		PropertyPlaceholderAutoConfiguration.class,
		ConfigurationPropertiesAutoConfiguration.class, JacksonAutoConfiguration.class,
		ReactiveWebServerFactoryAutoConfiguration.class, WebFluxAutoConfiguration.class,
		ErrorWebFluxAutoConfiguration.class, HttpHandlerAutoConfiguration.class })
@EntityScan
public class SampleApplication {

	private EntityManagerFactory entities;
	private boolean initialized;

	public SampleApplication(EntityManagerFactory entities) {
		this.entities = entities;
	}

	private void runner() {
		EntityManager manager = entities.createEntityManager();
		EntityTransaction transaction = manager.getTransaction();
		transaction.begin();
		Foo foo = manager.find(Foo.class, 1L);
		if (foo == null) {
			manager.persist(new Foo("Hello"));
		}
		transaction.commit();
	}

	@Bean
	public RouterFunction<?> userEndpoints() {
		if (!initialized) {
			runner();
			initialized = true;
		}
		return route(GET("/"),
				request -> ok().body(Mono
						.fromCallable(
								() -> entities.createEntityManager().find(Foo.class, 1L))
						.subscribeOn(Schedulers.elastic()), Foo.class));
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

}
