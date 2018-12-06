package org.springframework.boot.autoconfigure.thymeleaf;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import slim.ConditionService;

public class ThymeleafAutoConfiguration_DefaultTemplateResolverConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ThymeleafAutoConfiguration.DefaultTemplateResolverConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ThymeleafAutoConfiguration.DefaultTemplateResolverConfiguration.class).length==0) {
        context.registerBean(ThymeleafAutoConfiguration.DefaultTemplateResolverConfiguration.class, () -> new ThymeleafAutoConfiguration.DefaultTemplateResolverConfiguration(context.getBean(ThymeleafProperties.class),context));
        context.registerBean("defaultTemplateResolver", SpringResourceTemplateResolver.class, () -> context.getBean(ThymeleafAutoConfiguration.DefaultTemplateResolverConfiguration.class).defaultTemplateResolver());
      }
    }
  }
}
