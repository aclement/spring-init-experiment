package app.registrar;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

import lib.registrar.EnableBar;

@SpringBootConfiguration
@EnableBar
@Import({SampleApplicationModule.class, PropertyPlaceholderAutoConfiguration.class, ConfigurationPropertiesAutoConfiguration.class})
public class SampleApplication {
	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setApplicationContextClass(GenericApplicationContext.class);
		app.setDefaultProperties(Collections.singletonMap("spring.functional.enabled", "false"));
		app.setLogStartupInfo(false);
		app.run(args);
	}

}
