package org.springframework.boot.autoconfigure.data.mongo;

import java.lang.Class;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ImportRegistrars;

public class MongoReactiveRepositoriesAutoConfigureRegistrarInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    context.getBeanFactory().getBean(ImportRegistrars.class).add(MongoReactiveRepositoriesAutoConfigureRegistrar.class, MongoReactiveRepositoriesAutoConfigureRegistrar.class);
  }

  public static Class<?> configurations() {
    return MongoReactiveRepositoriesAutoConfigureRegistrar.class;
  }
}
