package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfigurationModule;

@SpringBootConfiguration
// TODO should import autoconfigs and get modules for free?
//@Import({MustacheAutoConfigurationModule.class, ReactiveWebServerFactoryAutoConfigurationModule.class})
@Import({MustacheAutoConfiguration.class, ReactiveWebServerFactoryAutoConfigurationModule.class})
public class SampleApplication {

	@Bean
	public SampleController controller() {
		return new SampleController();
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}
}
