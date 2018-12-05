package org.springframework.boot.autoconfigure.data.jdbc;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class JdbcRepositoriesAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JdbcRepositoriesAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(JdbcRepositoriesAutoConfiguration.class).length==0) {
        new JdbcRepositoriesAutoConfiguration_JdbcRepositoriesConfigurationInitializer().initialize(context);
        new JdbcRepositoriesAutoConfiguration_SpringBootJdbcConfigurationInitializer().initialize(context);
        context.registerBean(JdbcRepositoriesAutoConfiguration.class, () -> new JdbcRepositoriesAutoConfiguration());
      }
    }
  }
}
