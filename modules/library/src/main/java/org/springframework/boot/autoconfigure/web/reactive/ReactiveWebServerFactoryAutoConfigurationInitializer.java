package org.springframework.boot.autoconfigure.web.reactive;

import java.lang.Override;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class ReactiveWebServerFactoryAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ReactiveWebServerFactoryAutoConfiguration.class)) {
<<<<<<< HEAD
      if (context.getBeanFactory().getBeanNamesForType(ReactiveWebServerFactoryAutoConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(ReactiveWebServerFactoryAutoConfiguration.class, "org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar");
        new ReactiveWebServerFactoryConfiguration_EmbeddedJettyInitializer().initialize(context);
        new ReactiveWebServerFactoryConfiguration_EmbeddedUndertowInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(ReactiveWebServerFactoryAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        new ReactiveWebServerFactoryConfiguration_EmbeddedNettyInitializer().initialize(context);
        new ReactiveWebServerFactoryConfiguration_EmbeddedTomcatInitializer().initialize(context);
        context.registerBean(ReactiveWebServerFactoryAutoConfiguration.class, () -> new ReactiveWebServerFactoryAutoConfiguration());
        context.registerBean("reactiveWebServerFactoryCustomizer", ReactiveWebServerFactoryCustomizer.class, () -> context.getBean(ReactiveWebServerFactoryAutoConfiguration.class).reactiveWebServerFactoryCustomizer(context.getBean(ServerProperties.class)));
      }
=======
      new ReactiveWebServerFactoryConfiguration_EmbeddedUndertowInitializer().initialize(context);
      new ReactiveWebServerFactoryConfiguration_EmbeddedJettyInitializer().initialize(context);
      context.getBeanFactory().getBean(ImportRegistrars.class).add(ReactiveWebServerFactoryAutoConfiguration.class, "org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar");
      new ReactiveWebServerFactoryConfiguration_EmbeddedTomcatInitializer().initialize(context);
      context.getBeanFactory().getBean(ImportRegistrars.class).add(ReactiveWebServerFactoryAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      new ReactiveWebServerFactoryConfiguration_EmbeddedNettyInitializer().initialize(context);
      if (context.getBeanFactory().getBeanNamesForType(ReactiveWebServerFactoryAutoConfiguration.class).length==0) {
        context.registerBean(ReactiveWebServerFactoryAutoConfiguration.class, () -> new ReactiveWebServerFactoryAutoConfiguration());
      }
      context.registerBean("reactiveWebServerFactoryCustomizer", ReactiveWebServerFactoryCustomizer.class, () -> context.getBean(ReactiveWebServerFactoryAutoConfiguration.class).reactiveWebServerFactoryCustomizer(context.getBean(ServerProperties.class)));
>>>>>>> Add plain JDBC sample (db)
    }
  }
}
