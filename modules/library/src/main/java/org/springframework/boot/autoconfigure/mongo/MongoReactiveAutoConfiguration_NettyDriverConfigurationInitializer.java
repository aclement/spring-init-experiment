package org.springframework.boot.autoconfigure.mongo;

import com.mongodb.MongoClientSettings;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class MongoReactiveAutoConfiguration_NettyDriverConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MongoReactiveAutoConfiguration.NettyDriverConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(MongoReactiveAutoConfiguration.NettyDriverConfiguration.class).length==0) {
        context.registerBean(MongoReactiveAutoConfiguration.NettyDriverConfiguration.class, () -> new MongoReactiveAutoConfiguration.NettyDriverConfiguration());
        context.registerBean("nettyDriverCustomizer", MongoClientSettingsBuilderCustomizer.class, () -> context.getBean(MongoReactiveAutoConfiguration.NettyDriverConfiguration.class).nettyDriverCustomizer(context.getBeanProvider(MongoClientSettings.class)));
      }
    }
  }
}
