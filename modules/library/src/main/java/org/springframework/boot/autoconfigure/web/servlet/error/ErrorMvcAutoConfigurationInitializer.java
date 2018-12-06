package org.springframework.boot.autoconfigure.web.servlet.error;

import java.lang.Override;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class ErrorMvcAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ErrorMvcAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ErrorMvcAutoConfiguration.class).length==0) {
        new ErrorMvcAutoConfiguration_DefaultErrorViewResolverConfigurationInitializer().initialize(context);
        new ErrorMvcAutoConfiguration_WhitelabelErrorViewConfigurationInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(ErrorMvcAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(ErrorMvcAutoConfiguration.class, () -> new ErrorMvcAutoConfiguration(context.getBean(ServerProperties.class),context.getBean(DispatcherServletPath.class),context.getBeanProvider(ErrorViewResolver.class)));
        if (conditions.matches(ErrorMvcAutoConfiguration.class, DefaultErrorAttributes.class)) {
          context.registerBean("errorAttributes", DefaultErrorAttributes.class, () -> context.getBean(ErrorMvcAutoConfiguration.class).errorAttributes());
        }
        if (conditions.matches(ErrorMvcAutoConfiguration.class, BasicErrorController.class)) {
          context.registerBean("basicErrorController", BasicErrorController.class, () -> context.getBean(ErrorMvcAutoConfiguration.class).basicErrorController(context.getBean(ErrorAttributes.class)));
        }
        context.registerBean("errorPageCustomizer", ErrorPageRegistrar.class, () -> context.getBean(ErrorMvcAutoConfiguration.class).errorPageCustomizer());
        context.registerBean("preserveErrorControllerTargetClassPostProcessor", ErrorMvcAutoConfiguration.PreserveErrorControllerTargetClassPostProcessor.class, () -> context.getBean(ErrorMvcAutoConfiguration.class).preserveErrorControllerTargetClassPostProcessor());
      }
    }
  }
}
