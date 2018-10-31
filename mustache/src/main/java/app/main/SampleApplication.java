package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import boot.autoconfigure.mustache.MustacheAutoConfigurationModule;
import boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfigurationModule;

@SpringBootConfiguration
@Import({ SampleApplicationModule.class, MustacheAutoConfigurationModule.class,
		ReactiveWebServerFactoryAutoConfigurationModule.class })
public class SampleApplication {

	@Bean
	public SampleController controller() {
		return new SampleController();
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}
}
