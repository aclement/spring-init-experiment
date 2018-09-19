package app.enable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.support.GenericApplicationContext;

import boot.autoconfigure.context.ContextAutoConfigurationModule;
import lib.enable.EnableBar;
import slim.ImportModule;

@SpringBootConfiguration
@EnableBar
// TODO: make this work with `@Import(ConfigurationPropertiesAutoConfiguration.class)`
@ImportModule(module = ContextAutoConfigurationModule.class)
public class SampleApplication {
	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setApplicationContextClass(GenericApplicationContext.class);
		app.setLogStartupInfo(false);
		app.run(args);
	}

}
