package app.importer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

import boot.autoconfigure.context.ContextAutoConfigurationModule;
import lib.imported.Bar;
import lib.importer.SomeConfiguration;

@SpringBootConfiguration
@Import({ SomeConfiguration.class, ContextAutoConfigurationModule.class })
public class SampleApplication {
	
	@Autowired
	Bar bar;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setLogStartupInfo(false);
		app.run(args);
	}

}
