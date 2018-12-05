package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Override;
import javax.sql.DataSource;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class DataSourceInitializerInvokerInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(DataSourceInitializerInvoker.class).length==0) {
      context.registerBean(DataSourceInitializerInvoker.class, () -> new DataSourceInitializerInvoker(context.getBeanProvider(DataSource.class),context.getBean(DataSourceProperties.class),context));
    }
  }
}
