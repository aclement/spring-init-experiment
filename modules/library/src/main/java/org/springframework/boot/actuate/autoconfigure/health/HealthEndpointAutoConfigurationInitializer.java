package org.springframework.boot.actuate.autoconfigure.health;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ImportRegistrars;

public class HealthEndpointAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(HealthEndpointAutoConfiguration.class).length==0) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(HealthEndpointAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      new HealthEndpointConfigurationInitializer().initialize(context);
      new HealthEndpointWebExtensionConfigurationInitializer().initialize(context);
      context.registerBean(HealthEndpointAutoConfiguration.class, () -> new HealthEndpointAutoConfiguration());
    }
  }
}
