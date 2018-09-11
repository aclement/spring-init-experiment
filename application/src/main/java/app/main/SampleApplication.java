package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

import boot.generated.AutoConfiguration;

@SpringBootConfiguration
@Import({AutoConfiguration.class, SampleConfiguration.class})
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SampleApplication.class);
        app.setApplicationContextClass(GenericApplicationContext.class);
        app.run(args);
    }
}
