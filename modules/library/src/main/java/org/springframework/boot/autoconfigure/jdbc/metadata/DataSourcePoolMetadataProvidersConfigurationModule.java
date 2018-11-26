package org.springframework.boot.autoconfigure.jdbc.metadata;

import java.lang.Class;
import java.lang.Override;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.Module;

public class DataSourcePoolMetadataProvidersConfigurationModule implements Module {
  @Override
  public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
    return Arrays.asList(new DataSourcePoolMetadataProvidersConfigurationInitializer());
  }

  @Override
  public Class getRoot() {
    return DataSourcePoolMetadataProvidersConfiguration.class;
  }
}
