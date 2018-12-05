package org.springframework.boot.autoconfigure.jdbc.metadata;

import java.lang.Override;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class DataSourcePoolMetadataProvidersConfiguration_TomcatDataSourcePoolMetadataProviderConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DataSourcePoolMetadataProvidersConfiguration.TomcatDataSourcePoolMetadataProviderConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(DataSourcePoolMetadataProvidersConfiguration.TomcatDataSourcePoolMetadataProviderConfiguration.class).length==0) {
        context.registerBean(DataSourcePoolMetadataProvidersConfiguration.TomcatDataSourcePoolMetadataProviderConfiguration.class, () -> new DataSourcePoolMetadataProvidersConfiguration.TomcatDataSourcePoolMetadataProviderConfiguration());
        context.registerBean("tomcatPoolDataSourceMetadataProvider", DataSourcePoolMetadataProvider.class, () -> context.getBean(DataSourcePoolMetadataProvidersConfiguration.TomcatDataSourcePoolMetadataProviderConfiguration.class).tomcatPoolDataSourceMetadataProvider());
      }
    }
  }
}
