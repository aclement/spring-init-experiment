package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@Import({ SampleConfiguration.class,
		ConfigurationPropertiesAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class })
public class SampleApplication {

	public static void main(String[] args) {
		// TODO: remove custom subclass when Spring Boot supports more flexible custom
		// bean definition loaders
		SpringApplication app = new SpringApplication(SampleApplication.class) {
			@Override
			protected void load(ApplicationContext context, Object[] sources) {
			}
		};
		app.setLogStartupInfo(false);
		app.run(args);
	}

}
