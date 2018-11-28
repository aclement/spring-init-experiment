package org.springframework.boot.autoconfigure.data.mongo;

import com.mongodb.reactivestreams.client.MongoClient;
import java.lang.Override;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class MongoReactiveDataAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MongoReactiveDataAutoConfiguration.class)) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(MongoReactiveDataAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      new MongoDataConfigurationInitializer().initialize(context);
      if (context.getBeanFactory().getBeanNamesForType(MongoReactiveDataAutoConfiguration.class).length==0) {
        context.registerBean(MongoReactiveDataAutoConfiguration.class, () -> new MongoReactiveDataAutoConfiguration(context.getBean(MongoProperties.class)));
      }
      if (conditions.matches(MongoReactiveDataAutoConfiguration.class, SimpleReactiveMongoDatabaseFactory.class)) {
        context.registerBean("reactiveMongoDatabaseFactory", SimpleReactiveMongoDatabaseFactory.class, () -> context.getBean(MongoReactiveDataAutoConfiguration.class).reactiveMongoDatabaseFactory(context.getBean(MongoClient.class)));
      }
      if (conditions.matches(MongoReactiveDataAutoConfiguration.class, ReactiveMongoTemplate.class)) {
        context.registerBean("reactiveMongoTemplate", ReactiveMongoTemplate.class, () -> context.getBean(MongoReactiveDataAutoConfiguration.class).reactiveMongoTemplate(context.getBean(ReactiveMongoDatabaseFactory.class),context.getBean(MongoConverter.class)));
      }
      if (conditions.matches(MongoReactiveDataAutoConfiguration.class, MappingMongoConverter.class)) {
        context.registerBean("mappingMongoConverter", MappingMongoConverter.class, () -> context.getBean(MongoReactiveDataAutoConfiguration.class).mappingMongoConverter(context.getBean(MongoMappingContext.class),context.getBean(MongoCustomConversions.class)));
      }
    }
  }
}
