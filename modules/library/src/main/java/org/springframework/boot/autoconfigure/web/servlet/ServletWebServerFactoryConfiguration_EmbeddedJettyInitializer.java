package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ServletWebServerFactoryConfiguration_EmbeddedJettyInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ServletWebServerFactoryConfiguration.EmbeddedJetty.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ServletWebServerFactoryConfiguration.EmbeddedJetty.class).length==0) {
        context.registerBean(ServletWebServerFactoryConfiguration.EmbeddedJetty.class, () -> new ServletWebServerFactoryConfiguration.EmbeddedJetty());
        context.registerBean("JettyServletWebServerFactory", JettyServletWebServerFactory.class, () -> context.getBean(ServletWebServerFactoryConfiguration.EmbeddedJetty.class).JettyServletWebServerFactory());
      }
    }
  }
}
