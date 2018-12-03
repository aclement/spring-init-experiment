package org.springframework.boot.autoconfigure.web.reactive;

import java.lang.Override;
import org.springframework.boot.web.embedded.undertow.UndertowReactiveWebServerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ReactiveWebServerFactoryConfiguration_EmbeddedUndertowInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ReactiveWebServerFactoryConfiguration.EmbeddedUndertow.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ReactiveWebServerFactoryConfiguration.EmbeddedUndertow.class).length==0) {
        context.registerBean(ReactiveWebServerFactoryConfiguration.EmbeddedUndertow.class, () -> new ReactiveWebServerFactoryConfiguration.EmbeddedUndertow());
      }
      context.registerBean("undertowReactiveWebServerFactory", UndertowReactiveWebServerFactory.class, () -> context.getBean(ReactiveWebServerFactoryConfiguration.EmbeddedUndertow.class).undertowReactiveWebServerFactory());
    }
  }
}
