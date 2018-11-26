package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Class;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ModuleMapping;

@ModuleMapping(
    module = DataSourceAutoConfigurationModule.class
)
public class DataSourceJmxConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceJmxConfiguration.class)) {
      new DataSourceJmxConfiguration_HikariInitializer().initialize(context);
      new DataSourceJmxConfiguration_TomcatDataSourceJmxConfigurationInitializer().initialize(context);
      context.registerBean(DataSourceJmxConfiguration.class, () -> new DataSourceJmxConfiguration());
    }
  }

  public static Class<?> configurations() {
    return DataSourceJmxConfiguration.class;
  }
}
