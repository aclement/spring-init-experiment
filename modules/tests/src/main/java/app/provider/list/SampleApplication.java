package app.provider.list;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ContextAutoConfigurationModule;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@Import({ SampleConfiguration.class,ContextAutoConfigurationModule.class})
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setLogStartupInfo(false);
		app.run(args);
	}

}
