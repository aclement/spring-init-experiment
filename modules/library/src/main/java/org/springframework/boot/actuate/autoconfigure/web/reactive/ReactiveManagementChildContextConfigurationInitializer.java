package org.springframework.boot.actuate.autoconfigure.web.reactive;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.server.reactive.HttpHandler;
import slim.ConditionService;
import slim.ImportRegistrars;

public class ReactiveManagementChildContextConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ReactiveManagementChildContextConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ReactiveManagementChildContextConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(ReactiveManagementChildContextConfiguration.class, "org.springframework.web.reactive.config.DelegatingWebFluxConfiguration");
        context.registerBean(ReactiveManagementChildContextConfiguration.class, () -> new ReactiveManagementChildContextConfiguration());
        context.registerBean("reactiveManagementWebServerFactoryCustomizer", ReactiveManagementChildContextConfiguration.ReactiveManagementWebServerFactoryCustomizer.class, () -> context.getBean(ReactiveManagementChildContextConfiguration.class).reactiveManagementWebServerFactoryCustomizer(context.getBeanFactory()));
        context.registerBean("httpHandler", HttpHandler.class, () -> context.getBean(ReactiveManagementChildContextConfiguration.class).httpHandler(context));
      }
    }
  }
}
