package app.params;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import boot.autoconfigure.context.ContextAutoConfigurationModule;

@SpringBootConfiguration
@Import({ SampleConfiguration.class, ContextAutoConfigurationModule.class})
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.run(args);
	}

}

@Configuration
class SampleConfiguration {

	@Bean
	public Bar bar(ApplicationContext context) {
		return new Bar(context!=null);
	}

}