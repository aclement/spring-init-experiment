package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Class;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class JdbcTemplateAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JdbcTemplateAutoConfiguration.class)) {
      context.registerBean(JdbcProperties.class, () -> new JdbcProperties());
      context.registerBean(JdbcTemplateAutoConfiguration.class, () -> new JdbcTemplateAutoConfiguration());
    }
  }

  public static Class<?> configurations() {
    return JdbcTemplateAutoConfiguration.class;
  }
}
