package org.springframework.boot.autoconfigure.jdbc.metadata;

import java.lang.Override;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import slim.Module;

@Import({DataSourcePoolMetadataProvidersConfiguration.class, DataSourcePoolMetadataProvidersConfiguration.CommonsDbcp2PoolDataSourceMetadataProviderConfiguration.class, DataSourcePoolMetadataProvidersConfiguration.HikariPoolDataSourceMetadataProviderConfiguration.class, DataSourcePoolMetadataProvidersConfiguration.TomcatDataSourcePoolMetadataProviderConfiguration.class})
public class DataSourcePoolMetadataProvidersConfigurationModule implements Module {
  @Override
  public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
    return Arrays.asList(new TomcatDataSourcePoolMetadataProviderConfigurationInitializer(), new CommonsDbcp2PoolDataSourceMetadataProviderConfigurationInitializer(), new HikariPoolDataSourceMetadataProviderConfigurationInitializer(), new DataSourcePoolMetadataProvidersConfigurationInitializer());
  }
}
