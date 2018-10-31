package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
// TODO should import autoconfigs and get modules for free?
@Import({MustacheAutoConfiguration.class, ReactiveWebServerFactoryAutoConfiguration.class, JacksonAutoConfiguration.class})
//@Import({MustacheAutoConfiguration.class, ReactiveWebServerFactoryAutoConfiguration.class, JacksonAutoConfiguration.class})
public class SampleApplication {

	@Bean
	public SampleController controller() {
		return new SampleController();
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}
}
