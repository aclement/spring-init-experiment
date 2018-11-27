package org.springframework.boot.autoconfigure.jdbc;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.StandardAnnotationMetadata;

public class DataSourceInitializationConfiguration_RegistrarInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    DataSourceInitializationConfiguration.Registrar registrar = new DataSourceInitializationConfiguration.Registrar();
    registrar.registerBeanDefinitions(new StandardAnnotationMetadata(DataSourceInitializationConfiguration.Registrar.class),context);
  }

  public static Class<?> configurations() {
    return DataSourceInitializationConfiguration.Registrar.class;
  }
}
