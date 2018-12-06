package org.springframework.boot.autoconfigure.web.embedded;

import java.lang.Override;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import slim.ConditionService;

public class EmbeddedWebServerFactoryCustomizerAutoConfiguration_JettyWebServerFactoryCustomizerConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(EmbeddedWebServerFactoryCustomizerAutoConfiguration.JettyWebServerFactoryCustomizerConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(EmbeddedWebServerFactoryCustomizerAutoConfiguration.JettyWebServerFactoryCustomizerConfiguration.class).length==0) {
        context.registerBean(EmbeddedWebServerFactoryCustomizerAutoConfiguration.JettyWebServerFactoryCustomizerConfiguration.class, () -> new EmbeddedWebServerFactoryCustomizerAutoConfiguration.JettyWebServerFactoryCustomizerConfiguration());
        context.registerBean("jettyWebServerFactoryCustomizer", JettyWebServerFactoryCustomizer.class, () -> context.getBean(EmbeddedWebServerFactoryCustomizerAutoConfiguration.JettyWebServerFactoryCustomizerConfiguration.class).jettyWebServerFactoryCustomizer(context.getBean(Environment.class),context.getBean(ServerProperties.class)));
      }
    }
  }
}
