package org.springframework.boot.autoconfigure.jdbc;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import slim.ConditionService;

public class JndiDataSourceAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JndiDataSourceAutoConfiguration.class)) {
      context.registerBean(DataSourceProperties.class, () -> new DataSourceProperties());
      context.registerBean(JndiDataSourceAutoConfiguration.class, () -> new JndiDataSourceAutoConfiguration(context));
      if (conditions.matches(JndiDataSourceAutoConfiguration.class, DataSource.class)) {
        context.registerBean("dataSource", DataSource.class, () -> context.getBean(JndiDataSourceAutoConfiguration.class).dataSource(context.getBean(DataSourceProperties.class)));
      }
    }
  }

  public static Class<?> configurations() {
    return JndiDataSourceAutoConfiguration.class;
  }
}
