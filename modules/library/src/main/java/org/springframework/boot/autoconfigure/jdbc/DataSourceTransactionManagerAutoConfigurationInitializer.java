package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class DataSourceTransactionManagerAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceTransactionManagerAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(DataSourceTransactionManagerAutoConfiguration.class).length==0) {
        new DataSourceTransactionManagerAutoConfiguration_DataSourceTransactionManagerConfigurationInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(DataSourceTransactionManagerAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(DataSourceTransactionManagerAutoConfiguration.class, () -> new DataSourceTransactionManagerAutoConfiguration());
      }
    }
  }
}
