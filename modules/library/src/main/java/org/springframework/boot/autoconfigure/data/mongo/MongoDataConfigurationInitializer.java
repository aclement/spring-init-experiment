package org.springframework.boot.autoconfigure.data.mongo;

import java.lang.Override;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import slim.ConditionService;

public class MongoDataConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(MongoDataConfiguration.class).length==0) {
      context.registerBean(MongoDataConfiguration.class, () -> new MongoDataConfiguration(context,context.getBean(MongoProperties.class)));
    }
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MongoDataConfiguration.class, MongoMappingContext.class)) {
      context.registerBean("mongoMappingContext", MongoMappingContext.class, () -> { try { return context.getBean(MongoDataConfiguration.class).mongoMappingContext(context.getBean(MongoCustomConversions.class)); } catch (Exception e) { throw new IllegalStateException(e); } });
    }
    if (conditions.matches(MongoDataConfiguration.class, MongoCustomConversions.class)) {
      context.registerBean("mongoCustomConversions", MongoCustomConversions.class, () -> context.getBean(MongoDataConfiguration.class).mongoCustomConversions());
    }
  }
}
