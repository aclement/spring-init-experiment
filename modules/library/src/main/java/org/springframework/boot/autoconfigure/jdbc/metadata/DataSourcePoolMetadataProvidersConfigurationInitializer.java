package org.springframework.boot.autoconfigure.jdbc.metadata;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class DataSourcePoolMetadataProvidersConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    new DataSourcePoolMetadataProvidersConfiguration_TomcatDataSourcePoolMetadataProviderConfigurationInitializer().initialize(context);
    new DataSourcePoolMetadataProvidersConfiguration_HikariPoolDataSourceMetadataProviderConfigurationInitializer().initialize(context);
    new DataSourcePoolMetadataProvidersConfiguration_CommonsDbcp2PoolDataSourceMetadataProviderConfigurationInitializer().initialize(context);
    context.registerBean(DataSourcePoolMetadataProvidersConfiguration.class, () -> new DataSourcePoolMetadataProvidersConfiguration());
  }

  public static Class<?> configurations() {
    return DataSourcePoolMetadataProvidersConfiguration.class;
  }
}
