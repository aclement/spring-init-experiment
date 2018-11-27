package org.springframework.boot.autoconfigure.jdbc;

import org.apache.tomcat.jdbc.pool.DataSource;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import slim.ConditionService;

public class DataSourceConfiguration_TomcatInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceConfiguration.Tomcat.class)) {
      context.registerBean(DataSourceConfiguration.Tomcat.class, () -> new DataSourceConfiguration.Tomcat());
      context.registerBean("dataSource", DataSource.class, () -> context.getBean(DataSourceConfiguration.Tomcat.class).dataSource(context.getBean(DataSourceProperties.class)));
    }
  }

  public static Class<?> configurations() {
    return DataSourceConfiguration.Tomcat.class;
  }
}
