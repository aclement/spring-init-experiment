package com.acme;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.support.GenericApplicationContext;

import plugin.SlimConfigurationInstaller;

public class SimpleApplication {

    public static void main(String[] args) throws Exception {
        GenericApplicationContext context = new GenericApplicationContext();
        new SlimConfigurationInstaller().initialize(context);
        context.registerBean(SampleApplication.class);
        context.registerBean(SampleConfiguration.class);
        context.refresh();
        context.getBean(CommandLineRunner.class).run(args);
    }
}
