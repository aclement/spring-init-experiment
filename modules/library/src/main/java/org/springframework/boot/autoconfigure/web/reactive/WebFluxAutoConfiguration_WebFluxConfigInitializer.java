package org.springframework.boot.autoconfigure.web.reactive;

import java.lang.Override;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.result.view.ViewResolver;
import slim.ImportRegistrars;

public class WebFluxAutoConfiguration_WebFluxConfigInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
<<<<<<< HEAD
    if (context.getBeanFactory().getBeanNamesForType(WebFluxAutoConfiguration.WebFluxConfig.class).length==0) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(WebFluxAutoConfiguration.WebFluxConfig.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      new WebFluxAutoConfiguration_EnableWebFluxConfigurationInitializer().initialize(context);
=======
    context.getBeanFactory().getBean(ImportRegistrars.class).add(WebFluxAutoConfiguration.WebFluxConfig.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
    new WebFluxAutoConfiguration_EnableWebFluxConfigurationInitializer().initialize(context);
    if (context.getBeanFactory().getBeanNamesForType(WebFluxAutoConfiguration.WebFluxConfig.class).length==0) {
>>>>>>> Add plain JDBC sample (db)
      context.registerBean(WebFluxAutoConfiguration.WebFluxConfig.class, () -> new WebFluxAutoConfiguration.WebFluxConfig(context.getBean(ResourceProperties.class),context.getBean(WebFluxProperties.class),context.getBeanFactory(),context.getBeanProvider(HandlerMethodArgumentResolver.class),context.getBeanProvider(CodecCustomizer.class),context.getBeanProvider(ResourceHandlerRegistrationCustomizer.class),context.getBeanProvider(ViewResolver.class)));
    }
  }
}
