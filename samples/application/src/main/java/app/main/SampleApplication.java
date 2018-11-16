package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import boot.autoconfigure.context.ContextAutoConfigurationModule;

@SpringBootConfiguration
@Import({ SampleApplicationModule.class, SampleConfiguration.class,
		ContextAutoConfigurationModule.class })
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
