package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ServletWebServerFactoryConfiguration_EmbeddedUndertowInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ServletWebServerFactoryConfiguration.EmbeddedUndertow.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ServletWebServerFactoryConfiguration.EmbeddedUndertow.class).length==0) {
        context.registerBean(ServletWebServerFactoryConfiguration.EmbeddedUndertow.class, () -> new ServletWebServerFactoryConfiguration.EmbeddedUndertow());
        context.registerBean("undertowServletWebServerFactory", UndertowServletWebServerFactory.class, () -> context.getBean(ServletWebServerFactoryConfiguration.EmbeddedUndertow.class).undertowServletWebServerFactory());
      }
    }
  }
}
