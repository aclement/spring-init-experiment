package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

import boot.autoconfigure.context.ContextAutoConfigurationModule;
import boot.autoconfigure.gson.GsonAutoConfigurationModule;
import slim.SlimConfiguration;
import slim.SlimConfigurationInstaller;

@SpringBootConfiguration
@Import({ ConfigurationPropertiesAutoConfiguration.class, GsonAutoConfiguration.class,
        SampleConfiguration.class })
@SlimConfiguration(module = { SampleModule.class, GsonAutoConfigurationModule.class,
        ContextAutoConfigurationModule.class })
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SampleApplication.class);
        // This would be in spring.factories in a real application
        app.addInitializers(new SlimConfigurationInstaller());
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
