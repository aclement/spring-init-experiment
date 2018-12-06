package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.util.PathMatcher;
import org.springframework.validation.Validator;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.support.CompositeUriComponentsContributor;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.util.UrlPathHelper;

public class WebMvcAutoConfiguration_EnableWebMvcConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).length==0) {
      context.registerBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class, () -> new WebMvcAutoConfiguration.EnableWebMvcConfiguration(context.getBeanProvider(WebMvcProperties.class),context.getBeanProvider(WebMvcRegistrations.class),context.getBeanFactory()));
      context.registerBean("mvcContentNegotiationManager", ContentNegotiationManager.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).mvcContentNegotiationManager());
      context.registerBean("mvcConversionService", FormattingConversionService.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).mvcConversionService());
      context.registerBean("mvcValidator", Validator.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).mvcValidator());
      context.registerBean("requestMappingHandlerAdapter", RequestMappingHandlerAdapter.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).requestMappingHandlerAdapter());
      context.registerBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).requestMappingHandlerMapping());
      context.registerBean("beanNameHandlerMapping", BeanNameUrlHandlerMapping.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).beanNameHandlerMapping());
      context.registerBean("defaultServletHandlerMapping", HandlerMapping.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).defaultServletHandlerMapping());
      context.registerBean("handlerExceptionResolver", HandlerExceptionResolver.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).handlerExceptionResolver());
      context.registerBean("httpRequestHandlerAdapter", HttpRequestHandlerAdapter.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).httpRequestHandlerAdapter());
      context.registerBean("mvcHandlerMappingIntrospector", HandlerMappingIntrospector.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).mvcHandlerMappingIntrospector());
      context.registerBean("mvcPathMatcher", PathMatcher.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).mvcPathMatcher());
      context.registerBean("mvcResourceUrlProvider", ResourceUrlProvider.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).mvcResourceUrlProvider());
      context.registerBean("mvcUriComponentsContributor", CompositeUriComponentsContributor.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).mvcUriComponentsContributor());
      context.registerBean("mvcUrlPathHelper", UrlPathHelper.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).mvcUrlPathHelper());
      context.registerBean("mvcViewResolver", ViewResolver.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).mvcViewResolver());
      context.registerBean("resourceHandlerMapping", HandlerMapping.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).resourceHandlerMapping());
      context.registerBean("simpleControllerHandlerAdapter", SimpleControllerHandlerAdapter.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).simpleControllerHandlerAdapter());
      context.registerBean("viewControllerHandlerMapping", HandlerMapping.class, () -> context.getBean(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class).viewControllerHandlerMapping());
    }
  }
}
