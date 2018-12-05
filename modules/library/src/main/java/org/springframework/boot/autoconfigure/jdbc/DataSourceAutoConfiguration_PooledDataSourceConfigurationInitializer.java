package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class DataSourceAutoConfiguration_PooledDataSourceConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceAutoConfiguration.PooledDataSourceConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(DataSourceAutoConfiguration.PooledDataSourceConfiguration.class).length==0) {
        new DataSourceConfiguration_Dbcp2Initializer().initialize(context);
        new DataSourceConfiguration_GenericInitializer().initialize(context);
        new DataSourceConfiguration_TomcatInitializer().initialize(context);
        new DataSourceJmxConfigurationInitializer().initialize(context);
        new DataSourceConfiguration_HikariInitializer().initialize(context);
        context.registerBean(DataSourceAutoConfiguration.PooledDataSourceConfiguration.class, () -> new DataSourceAutoConfiguration.PooledDataSourceConfiguration());
      }
    }
  }
}
