package org.springframework.boot.autoconfigure.integration;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class IntegrationAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(IntegrationAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(IntegrationAutoConfiguration.class).length==0) {
        new IntegrationAutoConfiguration_IntegrationConfigurationInitializer().initialize(context);
        new IntegrationAutoConfiguration_IntegrationJmxConfigurationInitializer().initialize(context);
        new IntegrationAutoConfiguration_IntegrationManagementConfigurationInitializer().initialize(context);
        new IntegrationAutoConfiguration_IntegrationComponentScanConfigurationInitializer().initialize(context);
        new IntegrationAutoConfiguration_IntegrationJdbcConfigurationInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(IntegrationAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        new IntegrationManagementConfiguration_EnableIntegrationManagementConfigurationInitializer().initialize(context);
        context.registerBean(IntegrationAutoConfiguration.class, () -> new IntegrationAutoConfiguration());
      }
    }
  }
}
