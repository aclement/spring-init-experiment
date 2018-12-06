package org.springframework.boot.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.boot.autoconfigure.http.HttpProperties;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import slim.ConditionService;
import slim.ImportRegistrars;

public class HttpEncodingAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(HttpEncodingAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(HttpEncodingAutoConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(HttpEncodingAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(HttpEncodingAutoConfiguration.class, () -> new HttpEncodingAutoConfiguration(context.getBean(HttpProperties.class)));
        if (conditions.matches(HttpEncodingAutoConfiguration.class, CharacterEncodingFilter.class)) {
          context.registerBean("characterEncodingFilter", CharacterEncodingFilter.class, () -> context.getBean(HttpEncodingAutoConfiguration.class).characterEncodingFilter());
        }
        context.registerBean("localeCharsetMappingsCustomizer", WebServerFactoryCustomizer.class, () -> context.getBean(HttpEncodingAutoConfiguration.class).localeCharsetMappingsCustomizer());
      }
    }
  }
}
