package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import slim.ConditionService;

public class WebMvcAutoConfigurationAdapter_FaviconConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.FaviconConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.FaviconConfiguration.class).length==0) {
        context.registerBean(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.FaviconConfiguration.class, () -> new WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.FaviconConfiguration(context.getBean(ResourceProperties.class)));
        context.registerBean("faviconHandlerMapping", SimpleUrlHandlerMapping.class, () -> context.getBean(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.FaviconConfiguration.class).faviconHandlerMapping());
        context.registerBean("faviconRequestHandler", ResourceHttpRequestHandler.class, () -> context.getBean(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.FaviconConfiguration.class).faviconRequestHandler());
      }
    }
  }
}
