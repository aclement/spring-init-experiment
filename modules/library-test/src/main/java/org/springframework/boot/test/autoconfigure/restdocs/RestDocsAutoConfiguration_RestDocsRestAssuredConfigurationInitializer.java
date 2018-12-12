package org.springframework.boot.test.autoconfigure.restdocs;

import io.restassured.specification.RequestSpecification;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.restdocs.RestDocumentationContextProvider;
import slim.ConditionService;
import slim.ImportRegistrars;

public class RestDocsAutoConfiguration_RestDocsRestAssuredConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(RestDocsAutoConfiguration.RestDocsRestAssuredConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(RestDocsAutoConfiguration.RestDocsRestAssuredConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(RestDocsAutoConfiguration.RestDocsRestAssuredConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(RestDocsAutoConfiguration.RestDocsRestAssuredConfiguration.class, () -> new RestDocsAutoConfiguration.RestDocsRestAssuredConfiguration());
        context.registerBean("restAssuredBuilderCustomizer", RestDocsRestAssuredBuilderCustomizer.class, () -> context.getBean(RestDocsAutoConfiguration.RestDocsRestAssuredConfiguration.class).restAssuredBuilderCustomizer(context.getBean(RestDocsProperties.class),context.getBean(RequestSpecification.class)));
        if (conditions.matches(RestDocsAutoConfiguration.RestDocsRestAssuredConfiguration.class, RequestSpecification.class)) {
          context.registerBean("restDocsRestAssuredConfigurer", RequestSpecification.class, () -> context.getBean(RestDocsAutoConfiguration.RestDocsRestAssuredConfiguration.class).restDocsRestAssuredConfigurer(context.getBeanProvider(RestDocsRestAssuredConfigurationCustomizer.class),context.getBean(RestDocumentationContextProvider.class)));
        }
      }
    }
  }
}
