package org.springframework.boot.autoconfigure.data.jpa;

import java.lang.Override;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.task.AsyncTaskExecutor;
import slim.ConditionService;
import slim.ImportRegistrars;

public class JpaRepositoriesAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JpaRepositoriesAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(JpaRepositoriesAutoConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(JpaRepositoriesAutoConfiguration.class, "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfigureRegistrar");
        context.registerBean(JpaRepositoriesAutoConfiguration.class, () -> new JpaRepositoriesAutoConfiguration());
        if (conditions.matches(JpaRepositoriesAutoConfiguration.class, EntityManagerFactoryBuilderCustomizer.class)) {
          context.registerBean("entityManagerFactoryBootstrapExecutorCustomizer", EntityManagerFactoryBuilderCustomizer.class, () -> context.getBean(JpaRepositoriesAutoConfiguration.class).entityManagerFactoryBootstrapExecutorCustomizer(context.getBeanProvider(AsyncTaskExecutor.class)));
        }
      }
    }
  }
}
