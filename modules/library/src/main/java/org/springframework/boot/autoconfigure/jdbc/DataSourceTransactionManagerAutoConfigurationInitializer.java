package org.springframework.boot.autoconfigure.jdbc;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import slim.ConditionService;

public class DataSourceTransactionManagerAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceTransactionManagerAutoConfiguration.class)) {
      new DataSourceTransactionManagerAutoConfiguration_DataSourceTransactionManagerConfigurationInitializer().initialize(context);
      context.registerBean(DataSourceProperties.class, () -> new DataSourceProperties());
      context.registerBean(DataSourceTransactionManagerAutoConfiguration.class, () -> new DataSourceTransactionManagerAutoConfiguration());
    }
  }

  public static Class<?> configurations() {
    return DataSourceTransactionManagerAutoConfiguration.class;
  }
}
