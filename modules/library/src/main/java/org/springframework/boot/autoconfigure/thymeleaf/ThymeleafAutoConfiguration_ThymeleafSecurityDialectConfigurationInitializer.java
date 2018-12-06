package org.springframework.boot.autoconfigure.thymeleaf;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;
import slim.ConditionService;

public class ThymeleafAutoConfiguration_ThymeleafSecurityDialectConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ThymeleafAutoConfiguration.ThymeleafSecurityDialectConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ThymeleafAutoConfiguration.ThymeleafSecurityDialectConfiguration.class).length==0) {
        context.registerBean(ThymeleafAutoConfiguration.ThymeleafSecurityDialectConfiguration.class, () -> new ThymeleafAutoConfiguration.ThymeleafSecurityDialectConfiguration());
        if (conditions.matches(ThymeleafAutoConfiguration.ThymeleafSecurityDialectConfiguration.class, SpringSecurityDialect.class)) {
          context.registerBean("securityDialect", SpringSecurityDialect.class, () -> context.getBean(ThymeleafAutoConfiguration.ThymeleafSecurityDialectConfiguration.class).securityDialect());
        }
      }
    }
  }
}
