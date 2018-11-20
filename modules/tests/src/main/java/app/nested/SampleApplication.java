package app.nested;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import boot.autoconfigure.context.ContextAutoConfigurationModule;

@SpringBootConfiguration
@Import({ContextAutoConfigurationModule.class })
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.run(args);
	}

	@Configuration
	protected static class SampleConfiguration {
		@Value("${app.value}")
		private String message;

		@Bean
		public Foo foo() {
			return new Foo();
		}

		@Bean
		public Bar bar(Foo foo) {
			return new Bar(foo);
		}

		@Bean
		public CommandLineRunner runner(Bar bar) {
			return args -> {
				System.out.println("Message: " + message);
				System.out.println("Bar: " + bar);
				System.out.println("Foo: " + bar.getFoo());
			};
		}
	}
}
