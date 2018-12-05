package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import slim.ImportRegistrars;

public class EmbeddedDataSourceConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(EmbeddedDataSourceConfiguration.class).length==0) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(EmbeddedDataSourceConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      context.registerBean(EmbeddedDataSourceConfiguration.class, () -> new EmbeddedDataSourceConfiguration(context.getBean(DataSourceProperties.class)));
      context.registerBean("dataSource", EmbeddedDatabase.class, () -> context.getBean(EmbeddedDataSourceConfiguration.class).dataSource());
    }
  }
}
