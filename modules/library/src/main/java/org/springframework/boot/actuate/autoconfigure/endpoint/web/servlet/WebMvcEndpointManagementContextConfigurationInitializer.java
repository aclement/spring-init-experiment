package org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet;

import java.lang.Override;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.ControllerEndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class WebMvcEndpointManagementContextConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(WebMvcEndpointManagementContextConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(WebMvcEndpointManagementContextConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(WebMvcEndpointManagementContextConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(WebMvcEndpointManagementContextConfiguration.class, () -> new WebMvcEndpointManagementContextConfiguration());
        if (conditions.matches(WebMvcEndpointManagementContextConfiguration.class, WebMvcEndpointHandlerMapping.class)) {
          context.registerBean("webEndpointServletHandlerMapping", WebMvcEndpointHandlerMapping.class, () -> context.getBean(WebMvcEndpointManagementContextConfiguration.class).webEndpointServletHandlerMapping(context.getBean(WebEndpointsSupplier.class),context.getBean(ServletEndpointsSupplier.class),context.getBean(ControllerEndpointsSupplier.class),context.getBean(EndpointMediaTypes.class),context.getBean(CorsEndpointProperties.class),context.getBean(WebEndpointProperties.class)));
        }
        if (conditions.matches(WebMvcEndpointManagementContextConfiguration.class, ControllerEndpointHandlerMapping.class)) {
          context.registerBean("controllerEndpointHandlerMapping", ControllerEndpointHandlerMapping.class, () -> context.getBean(WebMvcEndpointManagementContextConfiguration.class).controllerEndpointHandlerMapping(context.getBean(ControllerEndpointsSupplier.class),context.getBean(CorsEndpointProperties.class),context.getBean(WebEndpointProperties.class)));
        }
      }
    }
  }
}
