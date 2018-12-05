package org.springframework.boot.autoconfigure.web.reactive;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class HttpHandlerAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(HttpHandlerAutoConfiguration.class)) {
      new HttpHandlerAutoConfiguration_AnnotationConfigInitializer().initialize(context);
      if (context.getBeanFactory().getBeanNamesForType(HttpHandlerAutoConfiguration.class).length==0) {
        context.registerBean(HttpHandlerAutoConfiguration.class, () -> new HttpHandlerAutoConfiguration());
      }
    }
  }
}
