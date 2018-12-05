package org.springframework.boot.autoconfigure.web.reactive.error;

import java.lang.Override;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;
import slim.ConditionService;
import slim.ImportRegistrars;

public class ErrorWebFluxAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ErrorWebFluxAutoConfiguration.class)) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(ErrorWebFluxAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      if (context.getBeanFactory().getBeanNamesForType(ErrorWebFluxAutoConfiguration.class).length==0) {
        context.registerBean(ErrorWebFluxAutoConfiguration.class, () -> new ErrorWebFluxAutoConfiguration(context.getBean(ServerProperties.class),context.getBean(ResourceProperties.class),context.getBeanProvider(ViewResolver.class),context.getBean(ServerCodecConfigurer.class),context));
      }
      if (conditions.matches(ErrorWebFluxAutoConfiguration.class, ErrorWebExceptionHandler.class)) {
        context.registerBean("errorWebExceptionHandler", ErrorWebExceptionHandler.class, () -> context.getBean(ErrorWebFluxAutoConfiguration.class).errorWebExceptionHandler(context.getBean(ErrorAttributes.class)));
      }
      if (conditions.matches(ErrorWebFluxAutoConfiguration.class, DefaultErrorAttributes.class)) {
        context.registerBean("errorAttributes", DefaultErrorAttributes.class, () -> context.getBean(ErrorWebFluxAutoConfiguration.class).errorAttributes());
      }
    }
  }
}
