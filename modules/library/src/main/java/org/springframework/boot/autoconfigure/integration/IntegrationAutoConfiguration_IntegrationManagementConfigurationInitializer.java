package org.springframework.boot.autoconfigure.integration;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class IntegrationAutoConfiguration_IntegrationManagementConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(IntegrationAutoConfiguration.IntegrationManagementConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(IntegrationAutoConfiguration.IntegrationManagementConfiguration.class).length==0) {
        new IntegrationManagementConfiguration_EnableIntegrationManagementConfigurationInitializer().initialize(context);
        context.registerBean(IntegrationAutoConfiguration.IntegrationManagementConfiguration.class, () -> new IntegrationAutoConfiguration.IntegrationManagementConfiguration());
      }
    }
  }
}
