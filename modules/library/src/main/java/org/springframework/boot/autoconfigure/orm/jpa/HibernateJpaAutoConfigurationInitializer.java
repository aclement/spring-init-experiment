package org.springframework.boot.autoconfigure.orm.jpa;

import java.lang.Class;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class HibernateJpaAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(HibernateJpaAutoConfiguration.class)) {
      context.registerBean(JpaProperties.class, () -> new JpaProperties());
      context.registerBean(HibernateJpaAutoConfiguration.class, () -> new HibernateJpaAutoConfiguration());
    }
  }

  public static Class<?> configurations() {
    return HibernateJpaAutoConfiguration.class;
  }
}
