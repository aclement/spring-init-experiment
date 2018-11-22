package app.doubler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ContextAutoConfigurationModule;
import org.springframework.context.annotation.Import;

import lib.enable.EnableBar;
import lib.enabler.SomeConfiguration;

@SpringBootConfiguration
@EnableBar
@Import({ SomeConfiguration.class, ContextAutoConfigurationModule.class })
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setLogStartupInfo(false);
		app.run(args);
	}

}
