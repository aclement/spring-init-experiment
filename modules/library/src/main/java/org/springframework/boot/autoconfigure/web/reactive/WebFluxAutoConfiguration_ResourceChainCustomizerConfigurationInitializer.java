package org.springframework.boot.autoconfigure.web.reactive;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class WebFluxAutoConfiguration_ResourceChainCustomizerConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(WebFluxAutoConfiguration.ResourceChainCustomizerConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(WebFluxAutoConfiguration.ResourceChainCustomizerConfiguration.class).length==0) {
        context.registerBean(WebFluxAutoConfiguration.ResourceChainCustomizerConfiguration.class, () -> new WebFluxAutoConfiguration.ResourceChainCustomizerConfiguration());
<<<<<<< HEAD
        context.registerBean("resourceHandlerRegistrationCustomizer", ResourceChainResourceHandlerRegistrationCustomizer.class, () -> context.getBean(WebFluxAutoConfiguration.ResourceChainCustomizerConfiguration.class).resourceHandlerRegistrationCustomizer());
      }
=======
      }
      context.registerBean("resourceHandlerRegistrationCustomizer", ResourceChainResourceHandlerRegistrationCustomizer.class, () -> context.getBean(WebFluxAutoConfiguration.ResourceChainCustomizerConfiguration.class).resourceHandlerRegistrationCustomizer());
>>>>>>> Add plain JDBC sample (db)
    }
  }
}
