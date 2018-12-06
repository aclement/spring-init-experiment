package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ServletWebServerFactoryConfiguration_EmbeddedTomcatInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ServletWebServerFactoryConfiguration.EmbeddedTomcat.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ServletWebServerFactoryConfiguration.EmbeddedTomcat.class).length==0) {
        context.registerBean(ServletWebServerFactoryConfiguration.EmbeddedTomcat.class, () -> new ServletWebServerFactoryConfiguration.EmbeddedTomcat());
        context.registerBean("tomcatServletWebServerFactory", TomcatServletWebServerFactory.class, () -> context.getBean(ServletWebServerFactoryConfiguration.EmbeddedTomcat.class).tomcatServletWebServerFactory());
      }
    }
  }
}
