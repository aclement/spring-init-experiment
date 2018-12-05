package org.springframework.boot.autoconfigure.mongo.embedded;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class EmbeddedMongoAutoConfiguration_RuntimeConfigConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(EmbeddedMongoAutoConfiguration.RuntimeConfigConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(EmbeddedMongoAutoConfiguration.RuntimeConfigConfiguration.class).length==0) {
        context.registerBean(EmbeddedMongoAutoConfiguration.RuntimeConfigConfiguration.class, () -> new EmbeddedMongoAutoConfiguration.RuntimeConfigConfiguration());
        context.registerBean("embeddedMongoRuntimeConfig", IRuntimeConfig.class, () -> context.getBean(EmbeddedMongoAutoConfiguration.RuntimeConfigConfiguration.class).embeddedMongoRuntimeConfig());
      }
    }
  }
}
