package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
import org.springframework.boot.web.servlet.filter.OrderedHiddenHttpMethodFilter;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class WebMvcAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(WebMvcAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(WebMvcAutoConfiguration.class).length==0) {
        new WebMvcAutoConfiguration_WebMvcAutoConfigurationAdapterInitializer().initialize(context);
        new WebMvcAutoConfiguration_EnableWebMvcConfigurationInitializer().initialize(context);
        new WebMvcAutoConfiguration_ResourceChainCustomizerConfigurationInitializer().initialize(context);
        new WebMvcAutoConfigurationAdapter_FaviconConfigurationInitializer().initialize(context);
        context.registerBean(WebMvcAutoConfiguration.class, () -> new WebMvcAutoConfiguration());
        if (conditions.matches(WebMvcAutoConfiguration.class, OrderedHiddenHttpMethodFilter.class)) {
          context.registerBean("hiddenHttpMethodFilter", OrderedHiddenHttpMethodFilter.class, () -> context.getBean(WebMvcAutoConfiguration.class).hiddenHttpMethodFilter());
        }
        if (conditions.matches(WebMvcAutoConfiguration.class, OrderedFormContentFilter.class)) {
          context.registerBean("formContentFilter", OrderedFormContentFilter.class, () -> context.getBean(WebMvcAutoConfiguration.class).formContentFilter());
        }
      }
    }
  }
}
