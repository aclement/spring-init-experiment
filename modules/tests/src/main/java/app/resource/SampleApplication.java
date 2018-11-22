package app.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ContextAutoConfigurationModule;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@SpringBootConfiguration
@Import({ ContextAutoConfigurationModule.class })
@ImportResource("bar-config.xml")
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setLogStartupInfo(false);
		app.run(args);
	}

}
