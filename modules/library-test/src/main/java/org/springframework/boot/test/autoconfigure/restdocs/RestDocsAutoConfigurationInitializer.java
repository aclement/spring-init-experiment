package org.springframework.boot.test.autoconfigure.restdocs;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class RestDocsAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(RestDocsAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(RestDocsAutoConfiguration.class).length==0) {
        new RestDocsAutoConfiguration_RestDocsMockMvcConfigurationInitializer().initialize(context);
        new RestDocsAutoConfiguration_RestDocsRestAssuredConfigurationInitializer().initialize(context);
        new RestDocsAutoConfiguration_RestDocsWebTestClientConfigurationInitializer().initialize(context);
        context.registerBean(RestDocsAutoConfiguration.class, () -> new RestDocsAutoConfiguration());
      }
    }
  }
}
