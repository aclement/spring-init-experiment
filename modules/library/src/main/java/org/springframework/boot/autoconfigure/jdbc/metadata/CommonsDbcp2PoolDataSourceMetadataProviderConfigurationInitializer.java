package org.springframework.boot.autoconfigure.jdbc.metadata;

import java.lang.Class;
import java.lang.Override;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class CommonsDbcp2PoolDataSourceMetadataProviderConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourcePoolMetadataProvidersConfiguration.CommonsDbcp2PoolDataSourceMetadataProviderConfiguration.class)) {
      context.registerBean(DataSourcePoolMetadataProvidersConfiguration.CommonsDbcp2PoolDataSourceMetadataProviderConfiguration.class, () -> new DataSourcePoolMetadataProvidersConfiguration.CommonsDbcp2PoolDataSourceMetadataProviderConfiguration());
      context.registerBean("commonsDbcp2PoolDataSourceMetadataProvider", DataSourcePoolMetadataProvider.class, () -> context.getBean(DataSourcePoolMetadataProvidersConfiguration.CommonsDbcp2PoolDataSourceMetadataProviderConfiguration.class).commonsDbcp2PoolDataSourceMetadataProvider());
    }
  }

  public static Class<?> configurations() {
    return DataSourcePoolMetadataProvidersConfiguration.CommonsDbcp2PoolDataSourceMetadataProviderConfiguration.class;
  }
}
