package org.springframework.boot.test.autoconfigure.web.reactive;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.web.reactive.server.MockServerConfigurer;
import slim.ConditionService;

public class WebTestClientSecurityConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(WebTestClientSecurityConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(WebTestClientSecurityConfiguration.class).length==0) {
        context.registerBean(WebTestClientSecurityConfiguration.class, () -> new WebTestClientSecurityConfiguration());
        context.registerBean("mockServerConfigurer", MockServerConfigurer.class, () -> context.getBean(WebTestClientSecurityConfiguration.class).mockServerConfigurer());
      }
    }
  }
}
