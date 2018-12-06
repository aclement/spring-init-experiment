package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import javax.servlet.MultipartConfigElement;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import slim.ConditionService;
import slim.ImportRegistrars;

public class DispatcherServletAutoConfiguration_DispatcherServletRegistrationConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration.class).length==0) {
        new DispatcherServletAutoConfiguration_DispatcherServletConfigurationInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration.class, () -> new DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration(context.getBean(WebMvcProperties.class),context.getBeanProvider(MultipartConfigElement.class)));
        if (conditions.matches(DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration.class, DispatcherServletRegistrationBean.class)) {
          context.registerBean("dispatcherServletRegistration", DispatcherServletRegistrationBean.class, () -> context.getBean(DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration.class).dispatcherServletRegistration(context.getBean(DispatcherServlet.class)));
        }
      }
    }
  }
}
