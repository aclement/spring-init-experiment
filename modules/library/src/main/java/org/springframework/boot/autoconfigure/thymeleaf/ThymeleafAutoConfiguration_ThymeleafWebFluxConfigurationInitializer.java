package org.springframework.boot.autoconfigure.thymeleaf;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.thymeleaf.spring5.ISpringWebFluxTemplateEngine;
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveViewResolver;
import slim.ConditionService;

public class ThymeleafAutoConfiguration_ThymeleafWebFluxConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ThymeleafAutoConfiguration.ThymeleafWebFluxConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ThymeleafAutoConfiguration.ThymeleafWebFluxConfiguration.class).length==0) {
        context.registerBean(ThymeleafAutoConfiguration.ThymeleafWebFluxConfiguration.class, () -> new ThymeleafAutoConfiguration.ThymeleafWebFluxConfiguration(context.getBean(ThymeleafProperties.class)));
        if (conditions.matches(ThymeleafAutoConfiguration.ThymeleafWebFluxConfiguration.class, ThymeleafReactiveViewResolver.class)) {
          context.registerBean("thymeleafViewResolver", ThymeleafReactiveViewResolver.class, () -> context.getBean(ThymeleafAutoConfiguration.ThymeleafWebFluxConfiguration.class).thymeleafViewResolver(context.getBean(ISpringWebFluxTemplateEngine.class)));
        }
      }
    }
  }
}
