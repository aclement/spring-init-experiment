package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class ServletWebServerFactoryAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ServletWebServerFactoryAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ServletWebServerFactoryAutoConfiguration.class).length==0) {
        new ServletWebServerFactoryConfiguration_EmbeddedJettyInitializer().initialize(context);
        new ServletWebServerFactoryConfiguration_EmbeddedUndertowInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(ServletWebServerFactoryAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        new ServletWebServerFactoryConfiguration_EmbeddedTomcatInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(ServletWebServerFactoryAutoConfiguration.class, "org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar");
        context.registerBean(ServletWebServerFactoryAutoConfiguration.class, () -> new ServletWebServerFactoryAutoConfiguration());
        context.registerBean("servletWebServerFactoryCustomizer", ServletWebServerFactoryCustomizer.class, () -> context.getBean(ServletWebServerFactoryAutoConfiguration.class).servletWebServerFactoryCustomizer(context.getBean(ServerProperties.class)));
        if (conditions.matches(ServletWebServerFactoryAutoConfiguration.class, TomcatServletWebServerFactoryCustomizer.class)) {
          context.registerBean("tomcatServletWebServerFactoryCustomizer", TomcatServletWebServerFactoryCustomizer.class, () -> context.getBean(ServletWebServerFactoryAutoConfiguration.class).tomcatServletWebServerFactoryCustomizer(context.getBean(ServerProperties.class)));
        }
      }
    }
  }
}
