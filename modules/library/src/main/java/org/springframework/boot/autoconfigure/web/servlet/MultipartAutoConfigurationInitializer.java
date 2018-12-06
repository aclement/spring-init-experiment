package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import javax.servlet.MultipartConfigElement;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import slim.ConditionService;
import slim.ImportRegistrars;

public class MultipartAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MultipartAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(MultipartAutoConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(MultipartAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(MultipartAutoConfiguration.class, () -> new MultipartAutoConfiguration(context.getBean(MultipartProperties.class)));
        if (conditions.matches(MultipartAutoConfiguration.class, MultipartConfigElement.class)) {
          context.registerBean("multipartConfigElement", MultipartConfigElement.class, () -> context.getBean(MultipartAutoConfiguration.class).multipartConfigElement());
        }
        if (conditions.matches(MultipartAutoConfiguration.class, StandardServletMultipartResolver.class)) {
          context.registerBean("multipartResolver", StandardServletMultipartResolver.class, () -> context.getBean(MultipartAutoConfiguration.class).multipartResolver());
        }
      }
    }
  }
}
