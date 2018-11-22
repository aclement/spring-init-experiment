package app.main;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ContextAutoConfigurationModule;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfigurationModule;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfigurationModule;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfigurationModule;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfigurationModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@SpringBootConfiguration
@Import({ DataSourceAutoConfigurationModule.class,
		HibernateJpaAutoConfigurationModule.class,
		ReactiveWebServerFactoryAutoConfigurationModule.class,
		JacksonAutoConfigurationModule.class, ContextAutoConfigurationModule.class })
@EntityScan
public class SampleApplication {

	private EntityManagerFactory entities;

	public SampleApplication(EntityManagerFactory entities) {
		this.entities = entities;
	}

	@Bean
	public CommandLineRunner runner() {
		return args -> {
			EntityManager manager = entities.createEntityManager();
			EntityTransaction transaction = manager.getTransaction();
			transaction.begin();
			Foo foo = manager.find(Foo.class, 1L);
			if (foo == null) {
				manager.persist(new Foo("Hello"));
			}
			transaction.commit();
		};
	}

	@Bean
	public RouterFunction<?> userEndpoints() {
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
