package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Class;
import java.lang.Override;
import javax.sql.DataSource;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ModuleMapping;

@ModuleMapping(
    module = DataSourceAutoConfigurationModule.class
)
public class DataSourceConfiguration_GenericInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceConfiguration.Generic.class)) {
      context.registerBean(DataSourceConfiguration.Generic.class, () -> new DataSourceConfiguration.Generic());
      context.registerBean("dataSource", DataSource.class, () -> context.getBean(DataSourceConfiguration.Generic.class).dataSource(context.getBean(DataSourceProperties.class)));
    }
  }

  public static Class<?> configurations() {
    return DataSourceConfiguration.Generic.class;
  }
}
