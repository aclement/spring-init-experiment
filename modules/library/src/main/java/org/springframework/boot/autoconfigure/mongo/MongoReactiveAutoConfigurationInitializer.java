package org.springframework.boot.autoconfigure.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import slim.ConditionService;
import slim.ImportRegistrars;

public class MongoReactiveAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MongoReactiveAutoConfiguration.class)) {
      new MongoReactiveAutoConfiguration_NettyDriverConfigurationInitializer().initialize(context);
      context.getBeanFactory().getBean(ImportRegistrars.class).add(MongoReactiveAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      if (context.getBeanFactory().getBeanNamesForType(MongoReactiveAutoConfiguration.class).length==0) {
        context.registerBean(MongoReactiveAutoConfiguration.class, () -> new MongoReactiveAutoConfiguration(context.getBeanProvider(MongoClientSettings.class)));
      }
      if (conditions.matches(MongoReactiveAutoConfiguration.class, MongoClient.class)) {
        context.registerBean("reactiveStreamsMongoClient", MongoClient.class, () -> context.getBean(MongoReactiveAutoConfiguration.class).reactiveStreamsMongoClient(context.getBean(MongoProperties.class),context.getBean(Environment.class),context.getBeanProvider(MongoClientSettingsBuilderCustomizer.class)));
      }
    }
  }
}
