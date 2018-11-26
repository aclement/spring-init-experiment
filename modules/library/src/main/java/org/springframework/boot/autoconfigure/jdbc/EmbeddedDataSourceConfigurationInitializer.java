package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Class;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import slim.ModuleMapping;

@ModuleMapping(
    module = DataSourceAutoConfigurationModule.class
)
public class EmbeddedDataSourceConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    context.registerBean(DataSourceProperties.class, () -> new DataSourceProperties());
    context.registerBean(EmbeddedDataSourceConfiguration.class, () -> new EmbeddedDataSourceConfiguration(context.getBean(DataSourceProperties.class)));
    context.registerBean("dataSource", EmbeddedDatabase.class, () -> context.getBean(EmbeddedDataSourceConfiguration.class).dataSource());
  }

  public static Class<?> configurations() {
    return EmbeddedDataSourceConfiguration.class;
  }
}
