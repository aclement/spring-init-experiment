package org.springframework.boot.actuate.autoconfigure.endpoint.web;

import java.lang.Override;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointDiscoverer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import slim.ConditionService;

public class WebEndpointAutoConfiguration_WebEndpointServletConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(WebEndpointAutoConfiguration.WebEndpointServletConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(WebEndpointAutoConfiguration.WebEndpointServletConfiguration.class).length==0) {
        context.registerBean(WebEndpointAutoConfiguration.WebEndpointServletConfiguration.class, () -> new WebEndpointAutoConfiguration.WebEndpointServletConfiguration());
        if (conditions.matches(WebEndpointAutoConfiguration.WebEndpointServletConfiguration.class, ServletEndpointDiscoverer.class)) {
          context.registerBean("servletEndpointDiscoverer", ServletEndpointDiscoverer.class, () -> context.getBean(WebEndpointAutoConfiguration.WebEndpointServletConfiguration.class).servletEndpointDiscoverer(context,context.getBeanProvider(PathMapper.class),context.getBeanProvider(ResolvableType.forClassWithGenerics(EndpointFilter.class, EndpointFilter.class))));
        }
      }
    }
  }
}
