package org.springframework.boot.autoconfigure.orm.jpa;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import slim.ConditionService;

public class HibernateJpaAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(HibernateJpaAutoConfiguration.class)) {
      new HibernateJpaConfigurationInitializer().initialize(context);
      context.registerBean(HibernateJpaAutoConfiguration.class, () -> new HibernateJpaAutoConfiguration());
      context.registerBean(JpaProperties.class, () -> new JpaProperties());
    }
  }

  public static Class<?> configurations() {
    return HibernateJpaAutoConfiguration.class;
  }
}
