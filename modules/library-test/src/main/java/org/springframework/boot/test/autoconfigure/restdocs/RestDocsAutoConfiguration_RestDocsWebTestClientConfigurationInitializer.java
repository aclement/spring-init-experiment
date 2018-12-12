package org.springframework.boot.test.autoconfigure.restdocs;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentationConfigurer;
import slim.ConditionService;
import slim.ImportRegistrars;

public class RestDocsAutoConfiguration_RestDocsWebTestClientConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(RestDocsAutoConfiguration.RestDocsWebTestClientConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(RestDocsAutoConfiguration.RestDocsWebTestClientConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(RestDocsAutoConfiguration.RestDocsWebTestClientConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(RestDocsAutoConfiguration.RestDocsWebTestClientConfiguration.class, () -> new RestDocsAutoConfiguration.RestDocsWebTestClientConfiguration());
        if (conditions.matches(RestDocsAutoConfiguration.RestDocsWebTestClientConfiguration.class, WebTestClientRestDocumentationConfigurer.class)) {
          context.registerBean("restDocsWebTestClientConfigurer", WebTestClientRestDocumentationConfigurer.class, () -> context.getBean(RestDocsAutoConfiguration.RestDocsWebTestClientConfiguration.class).restDocsWebTestClientConfigurer(context.getBeanProvider(RestDocsWebTestClientConfigurationCustomizer.class),context.getBean(RestDocumentationContextProvider.class)));
        }
        context.registerBean("restDocumentationConfigurer", RestDocsWebTestClientBuilderCustomizer.class, () -> context.getBean(RestDocsAutoConfiguration.RestDocsWebTestClientConfiguration.class).restDocumentationConfigurer(context.getBean(RestDocsProperties.class),context.getBean(WebTestClientRestDocumentationConfigurer.class)));
      }
    }
  }
}
