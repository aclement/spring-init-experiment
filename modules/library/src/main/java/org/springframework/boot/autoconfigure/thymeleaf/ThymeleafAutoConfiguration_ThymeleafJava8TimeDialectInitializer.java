package org.springframework.boot.autoconfigure.thymeleaf;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import slim.ConditionService;

public class ThymeleafAutoConfiguration_ThymeleafJava8TimeDialectInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ThymeleafAutoConfiguration.ThymeleafJava8TimeDialect.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ThymeleafAutoConfiguration.ThymeleafJava8TimeDialect.class).length==0) {
        context.registerBean(ThymeleafAutoConfiguration.ThymeleafJava8TimeDialect.class, () -> new ThymeleafAutoConfiguration.ThymeleafJava8TimeDialect());
        if (conditions.matches(ThymeleafAutoConfiguration.ThymeleafJava8TimeDialect.class, Java8TimeDialect.class)) {
          context.registerBean("java8TimeDialect", Java8TimeDialect.class, () -> context.getBean(ThymeleafAutoConfiguration.ThymeleafJava8TimeDialect.class).java8TimeDialect());
        }
      }
    }
  }
}
