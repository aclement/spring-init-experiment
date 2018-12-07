package org.springframework.boot.actuate.autoconfigure.endpoint.web;

import java.lang.Override;
import org.springframework.boot.actuate.endpoint.web.ServletEndpointRegistrar;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ServletEndpointManagementContextConfiguration_WebMvcServletEndpointManagementContextConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ServletEndpointManagementContextConfiguration.WebMvcServletEndpointManagementContextConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ServletEndpointManagementContextConfiguration.WebMvcServletEndpointManagementContextConfiguration.class).length==0) {
        context.registerBean(ServletEndpointManagementContextConfiguration.WebMvcServletEndpointManagementContextConfiguration.class, () -> new ServletEndpointManagementContextConfiguration.WebMvcServletEndpointManagementContextConfiguration(context));
        context.registerBean("servletEndpointRegistrar", ServletEndpointRegistrar.class, () -> context.getBean(ServletEndpointManagementContextConfiguration.WebMvcServletEndpointManagementContextConfiguration.class).servletEndpointRegistrar(context.getBean(WebEndpointProperties.class),context.getBean(ServletEndpointsSupplier.class)));
      }
    }
  }
}
