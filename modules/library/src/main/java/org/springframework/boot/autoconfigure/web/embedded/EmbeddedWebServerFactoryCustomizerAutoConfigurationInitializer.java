package org.springframework.boot.autoconfigure.web.embedded;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class EmbeddedWebServerFactoryCustomizerAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(EmbeddedWebServerFactoryCustomizerAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(EmbeddedWebServerFactoryCustomizerAutoConfiguration.class).length==0) {
        new EmbeddedWebServerFactoryCustomizerAutoConfiguration_TomcatWebServerFactoryCustomizerConfigurationInitializer().initialize(context);
        new EmbeddedWebServerFactoryCustomizerAutoConfiguration_JettyWebServerFactoryCustomizerConfigurationInitializer().initialize(context);
        new EmbeddedWebServerFactoryCustomizerAutoConfiguration_UndertowWebServerFactoryCustomizerConfigurationInitializer().initialize(context);
        new EmbeddedWebServerFactoryCustomizerAutoConfiguration_NettyWebServerFactoryCustomizerConfigurationInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(EmbeddedWebServerFactoryCustomizerAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(EmbeddedWebServerFactoryCustomizerAutoConfiguration.class, () -> new EmbeddedWebServerFactoryCustomizerAutoConfiguration());
      }
    }
  }
}
