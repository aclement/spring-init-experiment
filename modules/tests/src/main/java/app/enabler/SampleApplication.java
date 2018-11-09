package app.enabler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

import boot.autoconfigure.context.ContextAutoConfigurationModule;
import lib.enabler.SomeConfigurationModule;

@SpringBootConfiguration
@Import({ SomeConfigurationModule.class, ContextAutoConfigurationModule.class })
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setLogStartupInfo(false);
		app.run(args);
	}

}
