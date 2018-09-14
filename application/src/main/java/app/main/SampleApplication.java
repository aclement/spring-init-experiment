package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

import boot.autoconfigure.gson.GsonAutoConfigurationModule;
import boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfigurationModule;
import slim.SlimConfiguration;

@SpringBootConfiguration
@Import({ SampleConfiguration.class, GsonAutoConfiguration.class,
		ReactiveWebServerFactoryAutoConfigurationModule.class })
// Generated:
@SlimConfiguration(module = { SampleModule.class, GsonAutoConfigurationModule.class,
		ReactiveWebServerFactoryAutoConfigurationModule.class })
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setApplicationContextClass(GenericApplicationContext.class);
		app.run(args);
	}

	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return new Initializer();
	}

	private static class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			context.registerBean(SampleApplication.class);
		}

	}
}
