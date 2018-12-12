package org.springframework.boot.test.autoconfigure.orm.jpa;

import java.lang.Override;
import javax.persistence.EntityManagerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class TestEntityManagerAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(TestEntityManagerAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(TestEntityManagerAutoConfiguration.class).length==0) {
        context.registerBean(TestEntityManagerAutoConfiguration.class, () -> new TestEntityManagerAutoConfiguration());
        if (conditions.matches(TestEntityManagerAutoConfiguration.class, TestEntityManager.class)) {
          context.registerBean("testEntityManager", TestEntityManager.class, () -> context.getBean(TestEntityManagerAutoConfiguration.class).testEntityManager(context.getBean(EntityManagerFactory.class)));
        }
      }
    }
  }
}
