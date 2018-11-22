package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Class;
import java.lang.Override;
import javax.sql.DataSource;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jmx.export.MBeanExporter;
import slim.ConditionService;

public class HikariJmxInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceJmxConfiguration.Hikari.class)) {
      context.registerBean(DataSourceJmxConfiguration.Hikari.class, () -> new DataSourceJmxConfiguration.Hikari(context.getBean(DataSource.class),context.getBeanProvider(MBeanExporter.class)));
    }
  }

  public static Class<?> configurations() {
    return DataSourceJmxConfiguration.Hikari.class;
  }
}
