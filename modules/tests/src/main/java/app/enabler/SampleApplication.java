package app.enabler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.context.annotation.Import;

//import boot.autoconfigure.context.ContextAutoConfigurationModule;
import lib.registrar.Bar;
import lib.enabler.SomeConfigurationModule;

@SpringBootConfiguration
@Import({ SomeConfigurationModule.class, ConfigurationPropertiesAutoConfiguration.class})//ContextAutoConfigurationModule.class })
public class SampleApplication {

	@Autowired
	private Bar bar;
	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setLogStartupInfo(false);
		app.run(args);
	}

}
