package org.springframework.boot.autoconfigure.thymeleaf;

import java.lang.Override;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ThymeleafAutoConfiguration_ThymeleafWebLayoutConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ThymeleafAutoConfiguration.ThymeleafWebLayoutConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ThymeleafAutoConfiguration.ThymeleafWebLayoutConfiguration.class).length==0) {
        context.registerBean(ThymeleafAutoConfiguration.ThymeleafWebLayoutConfiguration.class, () -> new ThymeleafAutoConfiguration.ThymeleafWebLayoutConfiguration());
        if (conditions.matches(ThymeleafAutoConfiguration.ThymeleafWebLayoutConfiguration.class, LayoutDialect.class)) {
          context.registerBean("layoutDialect", LayoutDialect.class, () -> context.getBean(ThymeleafAutoConfiguration.ThymeleafWebLayoutConfiguration.class).layoutDialect());
        }
      }
    }
  }
}
