package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Override;
import javax.sql.DataSource;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class JndiDataSourceAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JndiDataSourceAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(JndiDataSourceAutoConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(JndiDataSourceAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(JndiDataSourceAutoConfiguration.class, () -> new JndiDataSourceAutoConfiguration(context));
        if (conditions.matches(JndiDataSourceAutoConfiguration.class, DataSource.class)) {
          context.registerBean("dataSource", DataSource.class, () -> context.getBean(JndiDataSourceAutoConfiguration.class).dataSource(context.getBean(DataSourceProperties.class)));
        }
      }
    }
  }
}
