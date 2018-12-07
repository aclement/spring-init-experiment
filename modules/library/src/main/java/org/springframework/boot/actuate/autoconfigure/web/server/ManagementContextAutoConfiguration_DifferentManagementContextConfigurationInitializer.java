package org.springframework.boot.actuate.autoconfigure.web.server;

import java.lang.Override;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ManagementContextAutoConfiguration_DifferentManagementContextConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ManagementContextAutoConfiguration.DifferentManagementContextConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ManagementContextAutoConfiguration.DifferentManagementContextConfiguration.class).length==0) {
        context.registerBean(ManagementContextAutoConfiguration.DifferentManagementContextConfiguration.class, () -> new ManagementContextAutoConfiguration.DifferentManagementContextConfiguration(context,context.getBean(ManagementContextFactory.class)));
      }
    }
  }
}
