package org.springframework.boot.autoconfigure.data.mongo;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class MongoRepositoriesAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MongoRepositoriesAutoConfiguration.class)) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(MongoRepositoriesAutoConfiguration.class, "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfigureRegistrar");
      if (context.getBeanFactory().getBeanNamesForType(MongoRepositoriesAutoConfiguration.class).length==0) {
        context.registerBean(MongoRepositoriesAutoConfiguration.class, () -> new MongoRepositoriesAutoConfiguration());
      }
    }
  }
}
