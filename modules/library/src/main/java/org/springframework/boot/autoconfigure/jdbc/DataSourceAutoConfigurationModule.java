package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Class;
import java.lang.Override;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.Module;

public class DataSourceAutoConfigurationModule implements Module {
  @Override
  public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
    return Arrays.asList(new DataSourceTransactionManagerAutoConfigurationInitializer(), new DataSourceAutoConfigurationInitializer(), new JndiDataSourceAutoConfigurationInitializer(), new XADataSourceAutoConfigurationInitializer(), new JdbcTemplateAutoConfigurationInitializer());
  }

  @Override
  public Class getRoot() {
    return DataSourceAutoConfiguration.class;
  }
}
