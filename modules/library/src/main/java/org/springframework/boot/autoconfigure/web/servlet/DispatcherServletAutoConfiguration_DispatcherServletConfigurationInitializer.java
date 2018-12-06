package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.boot.autoconfigure.http.HttpProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import slim.ConditionService;
import slim.ImportRegistrars;

public class DispatcherServletAutoConfiguration_DispatcherServletConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DispatcherServletAutoConfiguration.DispatcherServletConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(DispatcherServletAutoConfiguration.DispatcherServletConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(DispatcherServletAutoConfiguration.DispatcherServletConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(DispatcherServletAutoConfiguration.DispatcherServletConfiguration.class, () -> new DispatcherServletAutoConfiguration.DispatcherServletConfiguration(context.getBean(HttpProperties.class),context.getBean(WebMvcProperties.class)));
        context.registerBean("dispatcherServlet", DispatcherServlet.class, () -> context.getBean(DispatcherServletAutoConfiguration.DispatcherServletConfiguration.class).dispatcherServlet());
        if (conditions.matches(DispatcherServletAutoConfiguration.DispatcherServletConfiguration.class, MultipartResolver.class)) {
          context.registerBean("multipartResolver", MultipartResolver.class, () -> context.getBean(DispatcherServletAutoConfiguration.DispatcherServletConfiguration.class).multipartResolver(context.getBean(MultipartResolver.class)));
        }
      }
    }
  }
}
