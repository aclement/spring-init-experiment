package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Override;
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvidersConfigurationInitializer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class DataSourceAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourceAutoConfiguration.class)) {
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> Update the library
      if (context.getBeanFactory().getBeanNamesForType(DataSourceAutoConfiguration.class).length==0) {
        new DataSourceAutoConfiguration_EmbeddedDatabaseConfigurationInitializer().initialize(context);
        new DataSourceAutoConfiguration_PooledDataSourceConfigurationInitializer().initialize(context);
        new DataSourceInitializationConfigurationInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(DataSourceAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        new DataSourcePoolMetadataProvidersConfigurationInitializer().initialize(context);
        context.registerBean(DataSourceAutoConfiguration.class, () -> new DataSourceAutoConfiguration());
      }
<<<<<<< HEAD
=======
      new DataSourceAutoConfiguration_EmbeddedDatabaseConfigurationInitializer().initialize(context);
      new DataSourceAutoConfiguration_PooledDataSourceConfigurationInitializer().initialize(context);
      new DataSourceInitializationConfigurationInitializer().initialize(context);
      new DataSourcePoolMetadataProvidersConfigurationInitializer().initialize(context);
      context.registerBean(DataSourceAutoConfiguration.class, () -> new DataSourceAutoConfiguration());
>>>>>>> Add plain JDBC sample (db)
=======
>>>>>>> Update the library
    }
  }
}
