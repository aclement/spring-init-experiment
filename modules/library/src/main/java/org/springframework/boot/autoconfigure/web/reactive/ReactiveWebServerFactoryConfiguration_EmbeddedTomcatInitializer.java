package org.springframework.boot.autoconfigure.web.reactive;

import java.lang.Override;
import org.springframework.boot.web.embedded.tomcat.TomcatReactiveWebServerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ReactiveWebServerFactoryConfiguration_EmbeddedTomcatInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ReactiveWebServerFactoryConfiguration.EmbeddedTomcat.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ReactiveWebServerFactoryConfiguration.EmbeddedTomcat.class).length==0) {
        context.registerBean(ReactiveWebServerFactoryConfiguration.EmbeddedTomcat.class, () -> new ReactiveWebServerFactoryConfiguration.EmbeddedTomcat());
      }
      context.registerBean("tomcatReactiveWebServerFactory", TomcatReactiveWebServerFactory.class, () -> context.getBean(ReactiveWebServerFactoryConfiguration.EmbeddedTomcat.class).tomcatReactiveWebServerFactory());
    }
  }
}
