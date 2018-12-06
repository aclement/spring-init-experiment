package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import slim.ConditionService;
import slim.ImportRegistrars;

public class WebMvcAutoConfiguration_WebMvcAutoConfigurationAdapterInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class).length==0) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      new WebMvcAutoConfiguration_EnableWebMvcConfigurationInitializer().initialize(context);
      new WebMvcAutoConfigurationAdapter_FaviconConfigurationInitializer().initialize(context);
      context.registerBean(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class, () -> new WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter(context.getBean(ResourceProperties.class),context.getBean(WebMvcProperties.class),context.getBeanFactory(),context.getBeanProvider(HttpMessageConverters.class),context.getBeanProvider(WebMvcAutoConfiguration.ResourceHandlerRegistrationCustomizer.class)));
      ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
      if (conditions.matches(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class, BeanNameViewResolver.class)) {
        context.registerBean("beanNameViewResolver", BeanNameViewResolver.class, () -> context.getBean(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class).beanNameViewResolver());
      }
      if (conditions.matches(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class, InternalResourceViewResolver.class)) {
        context.registerBean("defaultViewResolver", InternalResourceViewResolver.class, () -> context.getBean(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class).defaultViewResolver());
      }
      if (conditions.matches(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class, LocaleResolver.class)) {
        context.registerBean("localeResolver", LocaleResolver.class, () -> context.getBean(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class).localeResolver());
      }
      if (conditions.matches(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class, RequestContextFilter.class)) {
        context.registerBean("requestContextFilter", RequestContextFilter.class, () -> context.getBean(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class).requestContextFilter());
      }
      if (conditions.matches(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class, ContentNegotiatingViewResolver.class)) {
        context.registerBean("viewResolver", ContentNegotiatingViewResolver.class, () -> context.getBean(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class).viewResolver(context.getBeanFactory()));
      }
      context.registerBean("welcomePageHandlerMapping", WelcomePageHandlerMapping.class, () -> context.getBean(WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class).welcomePageHandlerMapping(context));
    }
  }
}
