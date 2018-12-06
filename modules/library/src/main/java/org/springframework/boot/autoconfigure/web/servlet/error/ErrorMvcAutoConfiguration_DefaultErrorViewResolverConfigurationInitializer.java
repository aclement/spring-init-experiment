package org.springframework.boot.autoconfigure.web.servlet.error;

import java.lang.Override;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ErrorMvcAutoConfiguration_DefaultErrorViewResolverConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(ErrorMvcAutoConfiguration.DefaultErrorViewResolverConfiguration.class).length==0) {
      context.registerBean(ErrorMvcAutoConfiguration.DefaultErrorViewResolverConfiguration.class, () -> new ErrorMvcAutoConfiguration.DefaultErrorViewResolverConfiguration(context,context.getBean(ResourceProperties.class)));
      ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
      if (conditions.matches(ErrorMvcAutoConfiguration.DefaultErrorViewResolverConfiguration.class, DefaultErrorViewResolver.class)) {
        context.registerBean("conventionErrorViewResolver", DefaultErrorViewResolver.class, () -> context.getBean(ErrorMvcAutoConfiguration.DefaultErrorViewResolverConfiguration.class).conventionErrorViewResolver());
      }
    }
  }
}
