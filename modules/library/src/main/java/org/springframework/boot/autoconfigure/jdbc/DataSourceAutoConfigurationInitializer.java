package org.springframework.boot.autoconfigure.jdbc;

import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvidersConfigurationInitializer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import slim.ConditionService;

public class DataSourceAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceAutoConfiguration.class)) {
      new DataSourceAutoConfiguration_EmbeddedDatabaseConfigurationInitializer().initialize(context);
      new DataSourceAutoConfiguration_PooledDataSourceConfigurationInitializer().initialize(context);
      new DataSourceInitializationConfigurationInitializer().initialize(context);
      new DataSourcePoolMetadataProvidersConfigurationInitializer().initialize(context);
      context.registerBean(DataSourceAutoConfiguration.class, () -> new DataSourceAutoConfiguration());
    }
  }

  public static Class<?> configurations() {
    return DataSourceAutoConfiguration.class;
  }
}
