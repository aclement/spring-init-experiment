package org.springframework.boot.actuate.autoconfigure.health;

import java.lang.Override;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.HealthIndicatorRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class HealthEndpointConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(HealthEndpointConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(HealthEndpointConfiguration.class).length==0) {
        context.registerBean(HealthEndpointConfiguration.class, () -> new HealthEndpointConfiguration());
        if (conditions.matches(HealthEndpointConfiguration.class, HealthEndpoint.class)) {
          context.registerBean("healthEndpoint", HealthEndpoint.class, () -> context.getBean(HealthEndpointConfiguration.class).healthEndpoint(context.getBean(HealthAggregator.class),context.getBean(HealthIndicatorRegistry.class)));
        }
      }
    }
  }
}
