package org.springframework.boot.actuate.autoconfigure.web.server;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import slim.ConditionService;

public class ManagementContextAutoConfiguration_SameManagementContextConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ManagementContextAutoConfiguration.SameManagementContextConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ManagementContextAutoConfiguration.SameManagementContextConfiguration.class).length==0) {
        new SameManagementContextConfiguration_EnableSameManagementContextConfigurationInitializer().initialize(context);
        context.registerBean(ManagementContextAutoConfiguration.SameManagementContextConfiguration.class, () -> new ManagementContextAutoConfiguration.SameManagementContextConfiguration(context.getBean(Environment.class)));
      }
    }
  }
}
