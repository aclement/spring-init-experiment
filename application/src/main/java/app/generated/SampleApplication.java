package app.generated;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

import boot.generated.AutoConfiguration;
import boot.generated.AutoConfigurationModule;
import slim.SlimConfiguration;
import slim.SlimConfigurationInstaller;

@SpringBootConfiguration
@Import({ AutoConfiguration.class, SampleConfiguration.class })
@SlimConfiguration(module = { SampleModule.class, AutoConfigurationModule.class })
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
