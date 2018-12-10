package org.springframework.boot.autoconfigure.integration;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ImportRegistrars;

public class IntegrationManagementConfiguration_EnableIntegrationManagementConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(IntegrationAutoConfiguration.IntegrationManagementConfiguration.EnableIntegrationManagementConfiguration.class).length==0) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(IntegrationAutoConfiguration.IntegrationManagementConfiguration.EnableIntegrationManagementConfiguration.class, "org.springframework.integration.config.IntegrationManagementConfiguration");
      context.registerBean(IntegrationAutoConfiguration.IntegrationManagementConfiguration.EnableIntegrationManagementConfiguration.class, () -> new IntegrationAutoConfiguration.IntegrationManagementConfiguration.EnableIntegrationManagementConfiguration());
    }
  }
}
