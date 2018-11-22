package org.springframework.boot.autoconfigure.jdbc.metadata;

import java.lang.Class;
import java.lang.Override;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class HikariPoolDataSourceMetadataProviderConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourcePoolMetadataProvidersConfiguration.HikariPoolDataSourceMetadataProviderConfiguration.class)) {
      context.registerBean(DataSourcePoolMetadataProvidersConfiguration.HikariPoolDataSourceMetadataProviderConfiguration.class, () -> new DataSourcePoolMetadataProvidersConfiguration.HikariPoolDataSourceMetadataProviderConfiguration());
      context.registerBean("hikariPoolDataSourceMetadataProvider", DataSourcePoolMetadataProvider.class, () -> context.getBean(DataSourcePoolMetadataProvidersConfiguration.HikariPoolDataSourceMetadataProviderConfiguration.class).hikariPoolDataSourceMetadataProvider());
    }
  }

  public static Class<?> configurations() {
    return DataSourcePoolMetadataProvidersConfiguration.HikariPoolDataSourceMetadataProviderConfiguration.class;
  }
}
