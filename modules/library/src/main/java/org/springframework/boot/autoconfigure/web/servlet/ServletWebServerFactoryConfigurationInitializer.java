package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class ServletWebServerFactoryConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(ServletWebServerFactoryConfiguration.class).length==0) {
      new ServletWebServerFactoryConfiguration_EmbeddedTomcatInitializer().initialize(context);
      new ServletWebServerFactoryConfiguration_EmbeddedJettyInitializer().initialize(context);
      new ServletWebServerFactoryConfiguration_EmbeddedUndertowInitializer().initialize(context);
      context.registerBean(ServletWebServerFactoryConfiguration.class, () -> new ServletWebServerFactoryConfiguration());
    }
  }
}
