package org.springframework.boot.actuate.autoconfigure.endpoint.web;

import java.lang.Override;
import org.springframework.boot.actuate.endpoint.web.ServletEndpointRegistrar;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ServletEndpointManagementContextConfiguration_JerseyServletEndpointManagementContextConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ServletEndpointManagementContextConfiguration.JerseyServletEndpointManagementContextConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ServletEndpointManagementContextConfiguration.JerseyServletEndpointManagementContextConfiguration.class).length==0) {
        context.registerBean(ServletEndpointManagementContextConfiguration.JerseyServletEndpointManagementContextConfiguration.class, () -> new ServletEndpointManagementContextConfiguration.JerseyServletEndpointManagementContextConfiguration(context));
        context.registerBean("servletEndpointRegistrar", ServletEndpointRegistrar.class, () -> context.getBean(ServletEndpointManagementContextConfiguration.JerseyServletEndpointManagementContextConfiguration.class).servletEndpointRegistrar(context.getBean(WebEndpointProperties.class),context.getBean(ServletEndpointsSupplier.class)));
      }
    }
  }
}
