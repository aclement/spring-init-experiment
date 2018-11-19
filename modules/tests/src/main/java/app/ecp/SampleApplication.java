package app.ecp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import boot.autoconfigure.context.ContextAutoConfigurationModule;

@SpringBootConfiguration
@Import({ SampleConfiguration.class,ContextAutoConfigurationModule.class})
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.setLogStartupInfo(false);
		app.run(args);
	}

	@Bean
	CommandLineRunner clr(Foo f) {
		return args -> {
			System.out.println("Foo: " + f);
			System.out.println("Foo value: " + f.getValue());
		};
	}
}
