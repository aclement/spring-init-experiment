package org.springframework.boot.autoconfigure.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import slim.ConditionService;
import slim.ImportRegistrars;

public class MongoAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MongoAutoConfiguration.class)) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(MongoAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      if (context.getBeanFactory().getBeanNamesForType(MongoAutoConfiguration.class).length==0) {
        context.registerBean(MongoAutoConfiguration.class, () -> new MongoAutoConfiguration(context.getBean(MongoProperties.class),context.getBeanProvider(MongoClientOptions.class),context.getBean(Environment.class)));
      }
      if (conditions.matches(MongoAutoConfiguration.class, MongoClient.class)) {
        context.registerBean("mongo", MongoClient.class, () -> context.getBean(MongoAutoConfiguration.class).mongo());
      }
    }
  }
}
