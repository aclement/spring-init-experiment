package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ImportRegistrars;

public class DataSourceInitializationConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(DataSourceInitializationConfiguration.class).length==0) {
      new DataSourceInitializerInvokerInitializer().initialize(context);
      context.getBeanFactory().getBean(ImportRegistrars.class).add(DataSourceInitializationConfiguration.class, "org.springframework.boot.autoconfigure.jdbc.DataSourceInitializationConfiguration.Registrar");
      context.registerBean(DataSourceInitializationConfiguration.class, () -> new DataSourceInitializationConfiguration());
    }
  }
}
