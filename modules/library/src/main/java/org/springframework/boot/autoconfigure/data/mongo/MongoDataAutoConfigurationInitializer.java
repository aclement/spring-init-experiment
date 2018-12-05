package org.springframework.boot.autoconfigure.data.mongo;

import com.mongodb.MongoClient;
import java.lang.Override;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoDbFactorySupport;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import slim.ConditionService;
import slim.ImportRegistrars;

public class MongoDataAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MongoDataAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(MongoDataAutoConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(MongoDataAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        new MongoDataConfigurationInitializer().initialize(context);
        context.registerBean(MongoDataAutoConfiguration.class, () -> new MongoDataAutoConfiguration(context.getBean(MongoProperties.class)));
        if (conditions.matches(MongoDataAutoConfiguration.class, MongoDbFactorySupport.class)) {
          context.registerBean("mongoDbFactory", MongoDbFactorySupport.class, () -> context.getBean(MongoDataAutoConfiguration.class).mongoDbFactory(context.getBeanProvider(MongoClient.class),context.getBeanProvider(com.mongodb.client.MongoClient.class)));
        }
        if (conditions.matches(MongoDataAutoConfiguration.class, MongoTemplate.class)) {
          context.registerBean("mongoTemplate", MongoTemplate.class, () -> context.getBean(MongoDataAutoConfiguration.class).mongoTemplate(context.getBean(MongoDbFactory.class),context.getBean(MongoConverter.class)));
        }
        if (conditions.matches(MongoDataAutoConfiguration.class, MappingMongoConverter.class)) {
          context.registerBean("mappingMongoConverter", MappingMongoConverter.class, () -> context.getBean(MongoDataAutoConfiguration.class).mappingMongoConverter(context.getBean(MongoDbFactory.class),context.getBean(MongoMappingContext.class),context.getBean(MongoCustomConversions.class)));
        }
        if (conditions.matches(MongoDataAutoConfiguration.class, GridFsTemplate.class)) {
          context.registerBean("gridFsTemplate", GridFsTemplate.class, () -> context.getBean(MongoDataAutoConfiguration.class).gridFsTemplate(context.getBean(MongoDbFactory.class),context.getBean(MongoTemplate.class)));
        }
      }
    }
  }
}
