package org.springframework.boot.actuate.autoconfigure.endpoint.web.reactive;

import java.lang.Override;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.reactive.ControllerEndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.reactive.WebFluxEndpointHandlerMapping;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class WebFluxEndpointManagementContextConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(WebFluxEndpointManagementContextConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(WebFluxEndpointManagementContextConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(WebFluxEndpointManagementContextConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(WebFluxEndpointManagementContextConfiguration.class, () -> new WebFluxEndpointManagementContextConfiguration());
        if (conditions.matches(WebFluxEndpointManagementContextConfiguration.class, WebFluxEndpointHandlerMapping.class)) {
          context.registerBean("webEndpointReactiveHandlerMapping", WebFluxEndpointHandlerMapping.class, () -> context.getBean(WebFluxEndpointManagementContextConfiguration.class).webEndpointReactiveHandlerMapping(context.getBean(WebEndpointsSupplier.class),context.getBean(ControllerEndpointsSupplier.class),context.getBean(EndpointMediaTypes.class),context.getBean(CorsEndpointProperties.class),context.getBean(WebEndpointProperties.class)));
        }
        if (conditions.matches(WebFluxEndpointManagementContextConfiguration.class, ControllerEndpointHandlerMapping.class)) {
          context.registerBean("controllerEndpointHandlerMapping", ControllerEndpointHandlerMapping.class, () -> context.getBean(WebFluxEndpointManagementContextConfiguration.class).controllerEndpointHandlerMapping(context.getBean(ControllerEndpointsSupplier.class),context.getBean(CorsEndpointProperties.class),context.getBean(WebEndpointProperties.class)));
        }
      }
    }
  }
}
