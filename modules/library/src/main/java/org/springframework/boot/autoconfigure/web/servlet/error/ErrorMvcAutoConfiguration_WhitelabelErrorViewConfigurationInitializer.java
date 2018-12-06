package org.springframework.boot.autoconfigure.web.servlet.error;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import slim.ConditionService;

public class ErrorMvcAutoConfiguration_WhitelabelErrorViewConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ErrorMvcAutoConfiguration.WhitelabelErrorViewConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ErrorMvcAutoConfiguration.WhitelabelErrorViewConfiguration.class).length==0) {
        context.registerBean(ErrorMvcAutoConfiguration.WhitelabelErrorViewConfiguration.class, () -> new ErrorMvcAutoConfiguration.WhitelabelErrorViewConfiguration());
        if (conditions.matches(ErrorMvcAutoConfiguration.WhitelabelErrorViewConfiguration.class, BeanNameViewResolver.class)) {
          context.registerBean("beanNameViewResolver", BeanNameViewResolver.class, () -> context.getBean(ErrorMvcAutoConfiguration.WhitelabelErrorViewConfiguration.class).beanNameViewResolver());
        }
        if (conditions.matches(ErrorMvcAutoConfiguration.WhitelabelErrorViewConfiguration.class, View.class)) {
          context.registerBean("defaultErrorView", View.class, () -> context.getBean(ErrorMvcAutoConfiguration.WhitelabelErrorViewConfiguration.class).defaultErrorView());
        }
      }
    }
  }
}
