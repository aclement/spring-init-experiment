package org.springframework.boot.autoconfigure.data.mongo;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class MongoReactiveRepositoriesAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MongoReactiveRepositoriesAutoConfiguration.class)) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(MongoReactiveRepositoriesAutoConfiguration.class, "org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfigureRegistrar");
      if (context.getBeanFactory().getBeanNamesForType(MongoReactiveRepositoriesAutoConfiguration.class).length==0) {
        context.registerBean(MongoReactiveRepositoriesAutoConfiguration.class, () -> new MongoReactiveRepositoriesAutoConfiguration());
      }
    }
  }
}
