package org.springframework.boot.actuate.autoconfigure.health;

import java.lang.Override;
import org.springframework.boot.actuate.health.ApplicationHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicatorRegistry;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class HealthIndicatorAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(HealthIndicatorAutoConfiguration.class).length==0) {
      new HealthIndicatorAutoConfiguration_ReactiveHealthIndicatorConfigurationInitializer().initialize(context);
      context.getBeanFactory().getBean(ImportRegistrars.class).add(HealthIndicatorAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      context.registerBean(HealthIndicatorAutoConfiguration.class, () -> new HealthIndicatorAutoConfiguration(context.getBean(HealthIndicatorProperties.class)));
      ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
      if (conditions.matches(HealthIndicatorAutoConfiguration.class, ApplicationHealthIndicator.class)) {
        context.registerBean("applicationHealthIndicator", ApplicationHealthIndicator.class, () -> context.getBean(HealthIndicatorAutoConfiguration.class).applicationHealthIndicator());
      }
      if (conditions.matches(HealthIndicatorAutoConfiguration.class, OrderedHealthAggregator.class)) {
        context.registerBean("healthAggregator", OrderedHealthAggregator.class, () -> context.getBean(HealthIndicatorAutoConfiguration.class).healthAggregator());
      }
      if (conditions.matches(HealthIndicatorAutoConfiguration.class, HealthIndicatorRegistry.class)) {
        context.registerBean("healthIndicatorRegistry", HealthIndicatorRegistry.class, () -> context.getBean(HealthIndicatorAutoConfiguration.class).healthIndicatorRegistry(context));
      }
    }
  }
}
