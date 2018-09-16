package com.acme;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(List.class)
public class SampleConfiguration {

	@ConditionalOnMissingBean
    @Bean
    public Foo foo() {
        return new Foo();
    }

    @ConditionalOnClass(String.class)
    @Bean
    public Bar bar(Foo foo) {
        return new Bar(foo);
    }
    
    @Bean
    public CommandLineRunner runner(Bar bar) {
        return args -> {
            System.out.println("Bar: " + bar);
        };
    }
}
