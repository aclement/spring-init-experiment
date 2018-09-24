package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.context.annotation.Bean;

import boot.autoconfigure.mustache.MustacheAutoConfigurationModule;
import boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfigurationModule;
import slim.ImportModule;

@SpringBootConfiguration
@ImportModule(value= {MustacheAutoConfigurationModule.class, ReactiveWebServerFactoryAutoConfigurationModule.class})
public class SampleApplication {

	@Bean
	public SampleController controller() {
		return new SampleController();
	}

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setApplicationContextClass(ReactiveWebServerApplicationContext.class);
		app.run(args);
	}
}
