package org.springframework.boot.autoconfigure.jdbc;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import slim.ConditionService;

public class DataSourceTransactionManagerAutoConfiguration_DataSourceTransactionManagerConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceTransactionManagerAutoConfiguration.DataSourceTransactionManagerConfiguration.class)) {
      context.registerBean(DataSourceTransactionManagerAutoConfiguration.DataSourceTransactionManagerConfiguration.class, () -> new DataSourceTransactionManagerAutoConfiguration.DataSourceTransactionManagerConfiguration(context.getBean(DataSource.class),context.getBeanProvider(TransactionManagerCustomizers.class)));
      if (conditions.matches(DataSourceTransactionManagerAutoConfiguration.DataSourceTransactionManagerConfiguration.class, DataSourceTransactionManager.class)) {
        context.registerBean("transactionManager", DataSourceTransactionManager.class, () -> context.getBean(DataSourceTransactionManagerAutoConfiguration.DataSourceTransactionManagerConfiguration.class).transactionManager(context.getBean(DataSourceProperties.class)));
      }
    }
  }

  public static Class<?> configurations() {
    return DataSourceTransactionManagerAutoConfiguration.DataSourceTransactionManagerConfiguration.class;
  }
}
