package org.springframework.boot.actuate.autoconfigure.health;

import java.lang.Override;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthWebEndpointResponseMapper;
import org.springframework.boot.actuate.health.ReactiveHealthEndpointWebExtension;
import org.springframework.boot.actuate.health.ReactiveHealthIndicatorRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class HealthEndpointWebExtensionConfiguration_ReactiveWebHealthConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(HealthEndpointWebExtensionConfiguration.ReactiveWebHealthConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(HealthEndpointWebExtensionConfiguration.ReactiveWebHealthConfiguration.class).length==0) {
        context.registerBean(HealthEndpointWebExtensionConfiguration.ReactiveWebHealthConfiguration.class, () -> new HealthEndpointWebExtensionConfiguration.ReactiveWebHealthConfiguration(context.getBeanProvider(HealthAggregator.class),context.getBean(ReactiveHealthIndicatorRegistry.class)));
        if (conditions.matches(HealthEndpointWebExtensionConfiguration.ReactiveWebHealthConfiguration.class, ReactiveHealthEndpointWebExtension.class)) {
          context.registerBean("reactiveHealthEndpointWebExtension", ReactiveHealthEndpointWebExtension.class, () -> context.getBean(HealthEndpointWebExtensionConfiguration.ReactiveWebHealthConfiguration.class).reactiveHealthEndpointWebExtension(context.getBean(HealthWebEndpointResponseMapper.class)));
        }
      }
    }
  }
}
