package org.springframework.boot.actuate.autoconfigure.web.reactive;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ReactiveManagementContextAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ReactiveManagementContextAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ReactiveManagementContextAutoConfiguration.class).length==0) {
        context.registerBean(ReactiveManagementContextAutoConfiguration.class, () -> new ReactiveManagementContextAutoConfiguration());
        context.registerBean("reactiveWebChildContextFactory", ReactiveManagementContextFactory.class, () -> context.getBean(ReactiveManagementContextAutoConfiguration.class).reactiveWebChildContextFactory());
      }
    }
  }
}
