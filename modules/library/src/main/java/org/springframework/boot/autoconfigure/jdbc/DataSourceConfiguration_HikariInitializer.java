package org.springframework.boot.autoconfigure.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class DataSourceConfiguration_HikariInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceConfiguration.Hikari.class)) {
      if (context.getBeanFactory().getBeanNamesForType(DataSourceConfiguration.Hikari.class).length==0) {
        context.registerBean(DataSourceConfiguration.Hikari.class, () -> new DataSourceConfiguration.Hikari());
        context.registerBean("dataSource", HikariDataSource.class, () -> context.getBean(DataSourceConfiguration.Hikari.class).dataSource(context.getBean(DataSourceProperties.class)));
      }
    }
  }
}
