package org.springframework.boot.autoconfigure.web.embedded;

import java.lang.Override;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import slim.ConditionService;

public class EmbeddedWebServerFactoryCustomizerAutoConfiguration_UndertowWebServerFactoryCustomizerConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(EmbeddedWebServerFactoryCustomizerAutoConfiguration.UndertowWebServerFactoryCustomizerConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(EmbeddedWebServerFactoryCustomizerAutoConfiguration.UndertowWebServerFactoryCustomizerConfiguration.class).length==0) {
        context.registerBean(EmbeddedWebServerFactoryCustomizerAutoConfiguration.UndertowWebServerFactoryCustomizerConfiguration.class, () -> new EmbeddedWebServerFactoryCustomizerAutoConfiguration.UndertowWebServerFactoryCustomizerConfiguration());
        context.registerBean("undertowWebServerFactoryCustomizer", UndertowWebServerFactoryCustomizer.class, () -> context.getBean(EmbeddedWebServerFactoryCustomizerAutoConfiguration.UndertowWebServerFactoryCustomizerConfiguration.class).undertowWebServerFactoryCustomizer(context.getBean(Environment.class),context.getBean(ServerProperties.class)));
      }
    }
  }
}
