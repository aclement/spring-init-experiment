package org.springframework.boot.autoconfigure.jdbc.metadata;

import java.lang.Class;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class DataSourcePoolMetadataProvidersConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    context.registerBean(DataSourcePoolMetadataProvidersConfiguration.class, () -> new DataSourcePoolMetadataProvidersConfiguration());
  }

  public static Class<?> configurations() {
    return DataSourcePoolMetadataProvidersConfiguration.class;
  }
}
