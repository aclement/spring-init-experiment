package com.acme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.support.GenericApplicationContext;

@SpringBootConfiguration
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(new Class<?>[] {SampleApplication.class, SampleConfiguration.class});
        app.setApplicationContextClass(GenericApplicationContext.class);
        app.run(args);
    }
}
