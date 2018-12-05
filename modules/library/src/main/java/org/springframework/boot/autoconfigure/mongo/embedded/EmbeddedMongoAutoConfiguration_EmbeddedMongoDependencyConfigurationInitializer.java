package org.springframework.boot.autoconfigure.mongo.embedded;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class EmbeddedMongoAutoConfiguration_EmbeddedMongoDependencyConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(EmbeddedMongoAutoConfiguration.EmbeddedMongoDependencyConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(EmbeddedMongoAutoConfiguration.EmbeddedMongoDependencyConfiguration.class).length==0) {
        context.registerBean(EmbeddedMongoAutoConfiguration.EmbeddedMongoDependencyConfiguration.class, () -> new EmbeddedMongoAutoConfiguration.EmbeddedMongoDependencyConfiguration());
      }
    }
  }
}
