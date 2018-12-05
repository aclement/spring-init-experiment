package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class DataSourceAutoConfiguration_EmbeddedDatabaseConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceAutoConfiguration.EmbeddedDatabaseConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(DataSourceAutoConfiguration.EmbeddedDatabaseConfiguration.class).length==0) {
        new EmbeddedDataSourceConfigurationInitializer().initialize(context);
        context.registerBean(DataSourceAutoConfiguration.EmbeddedDatabaseConfiguration.class, () -> new DataSourceAutoConfiguration.EmbeddedDatabaseConfiguration());
      }
    }
  }
}
