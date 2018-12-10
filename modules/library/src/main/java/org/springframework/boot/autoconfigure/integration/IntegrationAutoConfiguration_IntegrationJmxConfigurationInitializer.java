package org.springframework.boot.autoconfigure.integration;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.monitor.IntegrationMBeanExporter;
import slim.ConditionService;

public class IntegrationAutoConfiguration_IntegrationJmxConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(IntegrationAutoConfiguration.IntegrationJmxConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(IntegrationAutoConfiguration.IntegrationJmxConfiguration.class).length==0) {
        context.registerBean(IntegrationAutoConfiguration.IntegrationJmxConfiguration.class, () -> new IntegrationAutoConfiguration.IntegrationJmxConfiguration());
        context.registerBean("integrationMbeanExporter", IntegrationMBeanExporter.class, () -> context.getBean(IntegrationAutoConfiguration.IntegrationJmxConfiguration.class).integrationMbeanExporter());
      }
    }
  }
}
