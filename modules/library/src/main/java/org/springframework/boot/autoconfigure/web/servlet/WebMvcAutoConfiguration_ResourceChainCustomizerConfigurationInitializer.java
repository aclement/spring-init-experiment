package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class WebMvcAutoConfiguration_ResourceChainCustomizerConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(WebMvcAutoConfiguration.ResourceChainCustomizerConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(WebMvcAutoConfiguration.ResourceChainCustomizerConfiguration.class).length==0) {
        context.registerBean(WebMvcAutoConfiguration.ResourceChainCustomizerConfiguration.class, () -> new WebMvcAutoConfiguration.ResourceChainCustomizerConfiguration());
        context.registerBean("resourceHandlerRegistrationCustomizer", WebMvcAutoConfiguration.ResourceChainResourceHandlerRegistrationCustomizer.class, () -> context.getBean(WebMvcAutoConfiguration.ResourceChainCustomizerConfiguration.class).resourceHandlerRegistrationCustomizer());
      }
    }
  }
}
