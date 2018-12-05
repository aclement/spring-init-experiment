package org.springframework.boot.autoconfigure.web.reactive;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.server.reactive.HttpHandler;

public class HttpHandlerAutoConfiguration_AnnotationConfigInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(HttpHandlerAutoConfiguration.AnnotationConfig.class).length==0) {
      context.registerBean(HttpHandlerAutoConfiguration.AnnotationConfig.class, () -> new HttpHandlerAutoConfiguration.AnnotationConfig(context));
    }
    context.registerBean("httpHandler", HttpHandler.class, () -> context.getBean(HttpHandlerAutoConfiguration.AnnotationConfig.class).httpHandler());
  }
}
