package org.springframework.boot.autoconfigure.web.reactive;

import java.lang.Override;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import slim.ConditionService;

public class ReactiveWebServerFactoryConfiguration_EmbeddedNettyInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ReactiveWebServerFactoryConfiguration.EmbeddedNetty.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ReactiveWebServerFactoryConfiguration.EmbeddedNetty.class).length==0) {
        context.registerBean(ReactiveWebServerFactoryConfiguration.EmbeddedNetty.class, () -> new ReactiveWebServerFactoryConfiguration.EmbeddedNetty());
<<<<<<< HEAD
        if (conditions.matches(ReactiveWebServerFactoryConfiguration.EmbeddedNetty.class, ReactorResourceFactory.class)) {
          context.registerBean("reactorServerResourceFactory", ReactorResourceFactory.class, () -> context.getBean(ReactiveWebServerFactoryConfiguration.EmbeddedNetty.class).reactorServerResourceFactory());
        }
        context.registerBean("nettyReactiveWebServerFactory", NettyReactiveWebServerFactory.class, () -> context.getBean(ReactiveWebServerFactoryConfiguration.EmbeddedNetty.class).nettyReactiveWebServerFactory(context.getBean(ReactorResourceFactory.class)));
=======
      }
      context.registerBean("nettyReactiveWebServerFactory", NettyReactiveWebServerFactory.class, () -> context.getBean(ReactiveWebServerFactoryConfiguration.EmbeddedNetty.class).nettyReactiveWebServerFactory(context.getBean(ReactorResourceFactory.class)));
      if (conditions.matches(ReactiveWebServerFactoryConfiguration.EmbeddedNetty.class, ReactorResourceFactory.class)) {
        context.registerBean("reactorServerResourceFactory", ReactorResourceFactory.class, () -> context.getBean(ReactiveWebServerFactoryConfiguration.EmbeddedNetty.class).reactorServerResourceFactory());
>>>>>>> Add plain JDBC sample (db)
      }
    }
  }
}
