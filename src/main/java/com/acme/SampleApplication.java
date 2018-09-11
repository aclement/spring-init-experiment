package com.acme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

@SpringBootConfiguration
@Import({ConfigurationPropertiesAutoConfiguration.class, SampleConfiguration.class})
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SampleApplication.class);
        app.setApplicationContextClass(GenericApplicationContext.class);
        app.run(args);
    }
}
