package org.springframework.boot.autoconfigure.orm.jpa;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class HibernateJpaAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(HibernateJpaAutoConfiguration.class)) {
<<<<<<< HEAD
      if (context.getBeanFactory().getBeanNamesForType(HibernateJpaAutoConfiguration.class).length==0) {
        new HibernateJpaConfigurationInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(HibernateJpaAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(HibernateJpaAutoConfiguration.class, () -> new HibernateJpaAutoConfiguration());
      }
=======
      new HibernateJpaConfigurationInitializer().initialize(context);
      context.registerBean(HibernateJpaAutoConfiguration.class, () -> new HibernateJpaAutoConfiguration());
>>>>>>> Add plain JDBC sample (db)
    }
  }
}
