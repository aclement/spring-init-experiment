package org.springframework.boot.actuate.autoconfigure.endpoint.web;

import java.lang.Override;
import org.springframework.boot.actuate.autoconfigure.endpoint.ExposeExcludePropertyEndpointFilter;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ServletEndpointManagementContextConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ServletEndpointManagementContextConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ServletEndpointManagementContextConfiguration.class).length==0) {
        new ServletEndpointManagementContextConfiguration_WebMvcServletEndpointManagementContextConfigurationInitializer().initialize(context);
        new ServletEndpointManagementContextConfiguration_JerseyServletEndpointManagementContextConfigurationInitializer().initialize(context);
        context.registerBean(ServletEndpointManagementContextConfiguration.class, () -> new ServletEndpointManagementContextConfiguration());
        context.registerBean("servletExposeExcludePropertyEndpointFilter", ExposeExcludePropertyEndpointFilter.class, () -> context.getBean(ServletEndpointManagementContextConfiguration.class).servletExposeExcludePropertyEndpointFilter(context.getBean(WebEndpointProperties.class)));
      }
    }
  }
}
