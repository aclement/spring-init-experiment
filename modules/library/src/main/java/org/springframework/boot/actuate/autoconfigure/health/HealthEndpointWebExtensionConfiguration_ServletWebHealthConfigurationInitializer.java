package org.springframework.boot.actuate.autoconfigure.health;

import java.lang.Override;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.HealthEndpointWebExtension;
import org.springframework.boot.actuate.health.HealthWebEndpointResponseMapper;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class HealthEndpointWebExtensionConfiguration_ServletWebHealthConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(HealthEndpointWebExtensionConfiguration.ServletWebHealthConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(HealthEndpointWebExtensionConfiguration.ServletWebHealthConfiguration.class).length==0) {
        context.registerBean(HealthEndpointWebExtensionConfiguration.ServletWebHealthConfiguration.class, () -> new HealthEndpointWebExtensionConfiguration.ServletWebHealthConfiguration());
        if (conditions.matches(HealthEndpointWebExtensionConfiguration.ServletWebHealthConfiguration.class, HealthEndpointWebExtension.class)) {
          context.registerBean("healthEndpointWebExtension", HealthEndpointWebExtension.class, () -> context.getBean(HealthEndpointWebExtensionConfiguration.ServletWebHealthConfiguration.class).healthEndpointWebExtension(context.getBean(HealthEndpoint.class),context.getBean(HealthWebEndpointResponseMapper.class)));
        }
      }
    }
  }
}
