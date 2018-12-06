package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class DispatcherServletAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DispatcherServletAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(DispatcherServletAutoConfiguration.class).length==0) {
        new DispatcherServletAutoConfiguration_DispatcherServletConfigurationInitializer().initialize(context);
        new DispatcherServletAutoConfiguration_DispatcherServletRegistrationConfigurationInitializer().initialize(context);
        context.registerBean(DispatcherServletAutoConfiguration.class, () -> new DispatcherServletAutoConfiguration());
      }
    }
  }
}
