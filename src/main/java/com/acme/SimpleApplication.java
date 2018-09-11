package com.acme;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class SimpleApplication {

    public static void main(String[] args) throws Exception {
        GenericApplicationContext context = new GenericApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(
                new PropertiesPropertySource("application", PropertiesLoaderUtils
                        .loadAllProperties("application.properties")));
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
        context.registerBean(SampleApplication.class);
        context.refresh();
        context.getBean(CommandLineRunner.class).run(args);
    }
}
