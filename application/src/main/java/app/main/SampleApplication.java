package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
//import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

import boot.autoconfigure.context.ContextAutoConfigurationModule;
import slim.ImportModule;

@SpringBootConfiguration
@Import({ SampleConfiguration.class })
@ImportModule(module = ContextAutoConfigurationModule.class)
public class SampleApplication {
	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setApplicationContextClass(GenericApplicationContext.class);
		app.setLogStartupInfo(false);
		app.run(args);
	}

}
