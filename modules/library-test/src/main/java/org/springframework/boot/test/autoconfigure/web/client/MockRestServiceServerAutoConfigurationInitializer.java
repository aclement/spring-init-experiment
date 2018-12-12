package org.springframework.boot.test.autoconfigure.web.client;

import java.lang.Override;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.web.client.MockRestServiceServer;
import slim.ConditionService;

public class MockRestServiceServerAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MockRestServiceServerAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(MockRestServiceServerAutoConfiguration.class).length==0) {
        context.registerBean(MockRestServiceServerAutoConfiguration.class, () -> new MockRestServiceServerAutoConfiguration());
        context.registerBean("mockServerRestTemplateCustomizer", MockServerRestTemplateCustomizer.class, () -> context.getBean(MockRestServiceServerAutoConfiguration.class).mockServerRestTemplateCustomizer());
        context.registerBean("mockRestServiceServer", MockRestServiceServer.class, () -> context.getBean(MockRestServiceServerAutoConfiguration.class).mockRestServiceServer(context.getBean(MockServerRestTemplateCustomizer.class)));
      }
    }
  }
}
