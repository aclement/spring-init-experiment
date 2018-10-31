package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

@SpringBootConfiguration
@Import({ SampleApplicationModule.class, SampleConfiguration.class,
		PropertyPlaceholderAutoConfiguration.class,
		ConfigurationPropertiesAutoConfiguration.class })
public class SampleApplication {

	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(SampleApplication.class);
		// TODO: remove this (https://github.com/spring-projects/spring-boot/issues/14589)
		app.setApplicationContextClass(GenericApplicationContext.class);
		app.setLogStartupInfo(false);
		app.run(args);
	}

}
