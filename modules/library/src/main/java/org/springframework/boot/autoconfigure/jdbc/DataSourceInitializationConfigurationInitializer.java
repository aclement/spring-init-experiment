package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Class;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ModuleMapping;

@ModuleMapping(
    module = DataSourceAutoConfigurationModule.class
)
public class DataSourceInitializationConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    new DataSourceInitializerInvokerInitializer().initialize(context);
    new DataSourceInitializationConfiguration_RegistrarInitializer().initialize(context);
    context.registerBean(DataSourceInitializationConfiguration.class, () -> new DataSourceInitializationConfiguration());
  }

  public static Class<?> configurations() {
    return DataSourceInitializationConfiguration.class;
  }
}
