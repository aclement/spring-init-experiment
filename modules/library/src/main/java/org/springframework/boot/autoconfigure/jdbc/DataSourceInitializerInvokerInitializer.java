package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Class;
import java.lang.Override;
import javax.sql.DataSource;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ModuleMapping;

@ModuleMapping(
    module = DataSourceAutoConfigurationModule.class
)
public class DataSourceInitializerInvokerInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    context.registerBean(DataSourceInitializerInvoker.class, () -> new DataSourceInitializerInvoker(context.getBeanProvider(DataSource.class),context.getBean(DataSourceProperties.class),context));
  }

  public static Class<?> configurations() {
    return DataSourceInitializerInvoker.class;
  }
}
