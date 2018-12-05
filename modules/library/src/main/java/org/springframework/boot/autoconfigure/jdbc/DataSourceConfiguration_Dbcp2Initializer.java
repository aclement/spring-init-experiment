package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Override;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class DataSourceConfiguration_Dbcp2Initializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceConfiguration.Dbcp2.class)) {
      if (context.getBeanFactory().getBeanNamesForType(DataSourceConfiguration.Dbcp2.class).length==0) {
        context.registerBean(DataSourceConfiguration.Dbcp2.class, () -> new DataSourceConfiguration.Dbcp2());
        context.registerBean("dataSource", BasicDataSource.class, () -> context.getBean(DataSourceConfiguration.Dbcp2.class).dataSource(context.getBean(DataSourceProperties.class)));
      }
    }
  }
}
