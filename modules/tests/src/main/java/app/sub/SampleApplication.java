package app.sub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

import boot.autoconfigure.context.ContextAutoConfigurationModule;
import lib.sub.BarConfiguration;
import lib.sub.runner.RunnerConfiguration;

@SpringBootConfiguration
@Import({
	BarConfiguration.class, RunnerConfiguration.class,
		ContextAutoConfigurationModule.class })
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setLogStartupInfo(false);
		app.run(args);
	}

}
