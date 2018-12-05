package org.springframework.boot.autoconfigure.mongo.embedded;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import java.lang.Override;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class EmbeddedMongoAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(EmbeddedMongoAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(EmbeddedMongoAutoConfiguration.class).length==0) {
        new EmbeddedMongoAutoConfiguration_RuntimeConfigConfigurationInitializer().initialize(context);
        new EmbeddedMongoAutoConfiguration_EmbeddedMongoDependencyConfigurationInitializer().initialize(context);
        new EmbeddedMongoAutoConfiguration_EmbeddedReactiveMongoDependencyConfigurationInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(EmbeddedMongoAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(EmbeddedMongoAutoConfiguration.class, () -> new EmbeddedMongoAutoConfiguration(context.getBean(MongoProperties.class),context.getBean(EmbeddedMongoProperties.class),context,context.getBean(IRuntimeConfig.class)));
        if (conditions.matches(EmbeddedMongoAutoConfiguration.class, MongodExecutable.class)) {
          context.registerBean("embeddedMongoServer", MongodExecutable.class, () -> { try { return context.getBean(EmbeddedMongoAutoConfiguration.class).embeddedMongoServer(context.getBean(IMongodConfig.class)); } catch (Exception e) { throw new IllegalStateException(e); } }, def -> {def.setInitMethodName("start"); def.setDestroyMethodName("stop");});
        }
        if (conditions.matches(EmbeddedMongoAutoConfiguration.class, IMongodConfig.class)) {
          context.registerBean("embeddedMongoConfiguration", IMongodConfig.class, () -> { try { return context.getBean(EmbeddedMongoAutoConfiguration.class).embeddedMongoConfiguration(); } catch (Exception e) { throw new IllegalStateException(e); } });
        }
      }
    }
  }
}
