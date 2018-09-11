package generated;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessorRegistrar;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

import slim.SlimConfiguration;
import slim.SlimConfigurationInstaller;

@SpringBootConfiguration
@Import({ ConfigurationPropertiesAutoConfiguration.class, SampleConfiguration.class })
@SlimConfiguration(module = SampleModule.class)
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
            // TODO: how to get from ConfigurationPropertiesAutoConfiguration to this?
            new ConfigurationPropertiesBindingPostProcessorRegistrar()
                    .registerBeanDefinitions(null, context);
        }

    }
}
