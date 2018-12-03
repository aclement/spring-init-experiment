package org.springframework.boot.autoconfigure.web.reactive;

import java.lang.Override;
import org.springframework.boot.web.reactive.filter.OrderedHiddenHttpMethodFilter;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class WebFluxAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(WebFluxAutoConfiguration.class)) {
<<<<<<< HEAD
      if (context.getBeanFactory().getBeanNamesForType(WebFluxAutoConfiguration.class).length==0) {
        new WebFluxAutoConfiguration_WebFluxConfigInitializer().initialize(context);
        new WebFluxAutoConfiguration_EnableWebFluxConfigurationInitializer().initialize(context);
        new WebFluxAutoConfiguration_ResourceChainCustomizerConfigurationInitializer().initialize(context);
        context.registerBean(WebFluxAutoConfiguration.class, () -> new WebFluxAutoConfiguration());
        if (conditions.matches(WebFluxAutoConfiguration.class, OrderedHiddenHttpMethodFilter.class)) {
          context.registerBean("hiddenHttpMethodFilter", OrderedHiddenHttpMethodFilter.class, () -> context.getBean(WebFluxAutoConfiguration.class).hiddenHttpMethodFilter());
        }
=======
      new WebFluxAutoConfiguration_WebFluxConfigInitializer().initialize(context);
      new WebFluxAutoConfiguration_EnableWebFluxConfigurationInitializer().initialize(context);
      new WebFluxAutoConfiguration_ResourceChainCustomizerConfigurationInitializer().initialize(context);
      if (context.getBeanFactory().getBeanNamesForType(WebFluxAutoConfiguration.class).length==0) {
        context.registerBean(WebFluxAutoConfiguration.class, () -> new WebFluxAutoConfiguration());
      }
      if (conditions.matches(WebFluxAutoConfiguration.class, OrderedHiddenHttpMethodFilter.class)) {
        context.registerBean("hiddenHttpMethodFilter", OrderedHiddenHttpMethodFilter.class, () -> context.getBean(WebFluxAutoConfiguration.class).hiddenHttpMethodFilter());
>>>>>>> Add plain JDBC sample (db)
      }
    }
  }
}
