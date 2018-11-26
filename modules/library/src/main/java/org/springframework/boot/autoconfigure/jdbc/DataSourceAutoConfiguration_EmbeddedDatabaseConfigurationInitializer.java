package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Class;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ModuleMapping;

@ModuleMapping(
    module = DataSourceAutoConfigurationModule.class
)
public class DataSourceAutoConfiguration_EmbeddedDatabaseConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceAutoConfiguration.EmbeddedDatabaseConfiguration.class)) {
      new EmbeddedDataSourceConfigurationInitializer().initialize(context);
      context.registerBean(DataSourceAutoConfiguration.EmbeddedDatabaseConfiguration.class, () -> new DataSourceAutoConfiguration.EmbeddedDatabaseConfiguration());
    }
  }

  public static Class<?> configurations() {
    return DataSourceAutoConfiguration.EmbeddedDatabaseConfiguration.class;
  }
}
