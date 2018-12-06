package org.springframework.boot.autoconfigure.thymeleaf;

import java.lang.Override;
import java.util.Collection;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring5.SpringWebFluxTemplateEngine;
import slim.ConditionService;

public class ThymeleafAutoConfiguration_ThymeleafReactiveConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ThymeleafAutoConfiguration.ThymeleafReactiveConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ThymeleafAutoConfiguration.ThymeleafReactiveConfiguration.class).length==0) {
        context.registerBean(ThymeleafAutoConfiguration.ThymeleafReactiveConfiguration.class, () -> new ThymeleafAutoConfiguration.ThymeleafReactiveConfiguration(context.getBean(ThymeleafProperties.class),context.getBean(Collection.class),context.getBeanProvider(IDialect.class)));
        if (conditions.matches(ThymeleafAutoConfiguration.ThymeleafReactiveConfiguration.class, SpringWebFluxTemplateEngine.class)) {
          context.registerBean("templateEngine", SpringWebFluxTemplateEngine.class, () -> context.getBean(ThymeleafAutoConfiguration.ThymeleafReactiveConfiguration.class).templateEngine());
        }
      }
    }
  }
}
