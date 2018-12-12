package org.springframework.boot.test.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class MockMvcSecurityConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MockMvcSecurityConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(MockMvcSecurityConfiguration.class).length==0) {
        context.registerBean(MockMvcSecurityConfiguration.class, () -> new MockMvcSecurityConfiguration());
        if (conditions.matches(MockMvcSecurityConfiguration.class, MockMvcSecurityConfiguration.SecurityMockMvcBuilderCustomizer.class)) {
          context.registerBean("securityMockMvcBuilderCustomizer", MockMvcSecurityConfiguration.SecurityMockMvcBuilderCustomizer.class, () -> context.getBean(MockMvcSecurityConfiguration.class).securityMockMvcBuilderCustomizer());
        }
      }
    }
  }
}
