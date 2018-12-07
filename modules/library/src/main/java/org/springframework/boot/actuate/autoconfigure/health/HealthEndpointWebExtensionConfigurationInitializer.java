package org.springframework.boot.actuate.autoconfigure.health;

import java.lang.Override;
import org.springframework.boot.actuate.health.HealthStatusHttpMapper;
import org.springframework.boot.actuate.health.HealthWebEndpointResponseMapper;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class HealthEndpointWebExtensionConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(HealthEndpointWebExtensionConfiguration.class).length==0) {
      new HealthEndpointWebExtensionConfiguration_ReactiveWebHealthConfigurationInitializer().initialize(context);
      new HealthEndpointWebExtensionConfiguration_ServletWebHealthConfigurationInitializer().initialize(context);
      context.getBeanFactory().getBean(ImportRegistrars.class).add(HealthEndpointWebExtensionConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      context.registerBean(HealthEndpointWebExtensionConfiguration.class, () -> new HealthEndpointWebExtensionConfiguration());
      ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
      if (conditions.matches(HealthEndpointWebExtensionConfiguration.class, HealthStatusHttpMapper.class)) {
        context.registerBean("createHealthStatusHttpMapper", HealthStatusHttpMapper.class, () -> context.getBean(HealthEndpointWebExtensionConfiguration.class).createHealthStatusHttpMapper(context.getBean(HealthIndicatorProperties.class)));
      }
      if (conditions.matches(HealthEndpointWebExtensionConfiguration.class, HealthWebEndpointResponseMapper.class)) {
        context.registerBean("healthWebEndpointResponseMapper", HealthWebEndpointResponseMapper.class, () -> context.getBean(HealthEndpointWebExtensionConfiguration.class).healthWebEndpointResponseMapper(context.getBean(HealthStatusHttpMapper.class),context.getBean(HealthEndpointProperties.class)));
      }
    }
  }
}
