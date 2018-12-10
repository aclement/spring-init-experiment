package org.springframework.boot.autoconfigure.integration;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class IntegrationAutoConfiguration_IntegrationComponentScanConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(IntegrationAutoConfiguration.IntegrationComponentScanConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(IntegrationAutoConfiguration.IntegrationComponentScanConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(IntegrationAutoConfiguration.IntegrationComponentScanConfiguration.class, "org.springframework.boot.autoconfigure.integration.IntegrationAutoConfigurationScanRegistrar");
        context.registerBean(IntegrationAutoConfiguration.IntegrationComponentScanConfiguration.class, () -> new IntegrationAutoConfiguration.IntegrationComponentScanConfiguration());
      }
    }
  }
}
