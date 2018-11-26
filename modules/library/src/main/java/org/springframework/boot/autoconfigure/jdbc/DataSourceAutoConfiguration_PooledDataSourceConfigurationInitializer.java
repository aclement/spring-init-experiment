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
public class DataSourceAutoConfiguration_PooledDataSourceConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceAutoConfiguration.PooledDataSourceConfiguration.class)) {
      new DataSourceConfiguration_Dbcp2Initializer().initialize(context);
      new DataSourceConfiguration_GenericInitializer().initialize(context);
      new DataSourceConfiguration_TomcatInitializer().initialize(context);
      new DataSourceJmxConfigurationInitializer().initialize(context);
      new DataSourceConfiguration_HikariInitializer().initialize(context);
      context.registerBean(DataSourceAutoConfiguration.PooledDataSourceConfiguration.class, () -> new DataSourceAutoConfiguration.PooledDataSourceConfiguration());
    }
  }

  public static Class<?> configurations() {
    return DataSourceAutoConfiguration.PooledDataSourceConfiguration.class;
  }
}
