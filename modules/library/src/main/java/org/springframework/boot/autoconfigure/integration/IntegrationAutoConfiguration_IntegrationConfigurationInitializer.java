package org.springframework.boot.autoconfigure.integration;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ImportRegistrars;

public class IntegrationAutoConfiguration_IntegrationConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(IntegrationAutoConfiguration.IntegrationConfiguration.class).length==0) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(IntegrationAutoConfiguration.IntegrationConfiguration.class, "org.springframework.integration.config.IntegrationRegistrar");
      context.registerBean(IntegrationAutoConfiguration.IntegrationConfiguration.class, () -> new IntegrationAutoConfiguration.IntegrationConfiguration());
    }
  }
}
