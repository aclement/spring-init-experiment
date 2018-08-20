package com.acme;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleConfiguration {

    @Bean
    public Foo foo() {
        return new Foo();
    }

    @Bean
    public Bar bar(Foo foo) {
        return new Bar(foo);
    }
    
    @Bean
    public CommandLineRunner runner(Bar bar) {
        return args -> {
            System.err.println("Bar: " + bar);
        };
    }
}
