package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Object;
import java.lang.Override;
import javax.sql.DataSource;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class DataSourceJmxConfiguration_TomcatDataSourceJmxConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceJmxConfiguration.TomcatDataSourceJmxConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(DataSourceJmxConfiguration.TomcatDataSourceJmxConfiguration.class).length==0) {
        context.registerBean(DataSourceJmxConfiguration.TomcatDataSourceJmxConfiguration.class, () -> new DataSourceJmxConfiguration.TomcatDataSourceJmxConfiguration());
        if (conditions.matches(DataSourceJmxConfiguration.TomcatDataSourceJmxConfiguration.class, Object.class)) {
          context.registerBean("dataSourceMBean", Object.class, () -> context.getBean(DataSourceJmxConfiguration.TomcatDataSourceJmxConfiguration.class).dataSourceMBean(context.getBean(DataSource.class)));
        }
      }
    }
  }
}
