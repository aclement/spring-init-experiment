package org.springframework.boot.autoconfigure.data.jdbc;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class JdbcRepositoriesAutoConfiguration_JdbcRepositoriesConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JdbcRepositoriesAutoConfiguration.JdbcRepositoriesConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(JdbcRepositoriesAutoConfiguration.JdbcRepositoriesConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(JdbcRepositoriesAutoConfiguration.JdbcRepositoriesConfiguration.class, "org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfigureRegistrar");
        context.registerBean(JdbcRepositoriesAutoConfiguration.JdbcRepositoriesConfiguration.class, () -> new JdbcRepositoriesAutoConfiguration.JdbcRepositoriesConfiguration());
      }
    }
  }
}
