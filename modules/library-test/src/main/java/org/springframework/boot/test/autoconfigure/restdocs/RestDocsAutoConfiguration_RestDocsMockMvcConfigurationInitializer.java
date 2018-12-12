package org.springframework.boot.test.autoconfigure.restdocs;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import slim.ConditionService;
import slim.ImportRegistrars;

public class RestDocsAutoConfiguration_RestDocsMockMvcConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(RestDocsAutoConfiguration.RestDocsMockMvcConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(RestDocsAutoConfiguration.RestDocsMockMvcConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(RestDocsAutoConfiguration.RestDocsMockMvcConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(RestDocsAutoConfiguration.RestDocsMockMvcConfiguration.class, () -> new RestDocsAutoConfiguration.RestDocsMockMvcConfiguration());
        if (conditions.matches(RestDocsAutoConfiguration.RestDocsMockMvcConfiguration.class, MockMvcRestDocumentationConfigurer.class)) {
          context.registerBean("restDocsMockMvcConfigurer", MockMvcRestDocumentationConfigurer.class, () -> context.getBean(RestDocsAutoConfiguration.RestDocsMockMvcConfiguration.class).restDocsMockMvcConfigurer(context.getBeanProvider(RestDocsMockMvcConfigurationCustomizer.class),context.getBean(RestDocumentationContextProvider.class)));
        }
        context.registerBean("restDocumentationConfigurer", RestDocsMockMvcBuilderCustomizer.class, () -> context.getBean(RestDocsAutoConfiguration.RestDocsMockMvcConfiguration.class).restDocumentationConfigurer(context.getBean(RestDocsProperties.class),context.getBean(MockMvcRestDocumentationConfigurer.class),context.getBeanProvider(RestDocumentationResultHandler.class)));
      }
    }
  }
}
