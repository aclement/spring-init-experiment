package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ContextAutoConfigurationModule;
import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfigurationModule;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfigurationModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@Import({ MustacheAutoConfigurationModule.class,
		ReactiveWebServerFactoryAutoConfigurationModule.class,
		ContextAutoConfigurationModule.class })
public class SampleApplication {

	@Bean
	public SampleController controller() {
		return new SampleController();
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}
}
