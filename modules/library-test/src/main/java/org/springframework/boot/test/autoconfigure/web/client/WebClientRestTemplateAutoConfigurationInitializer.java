package org.springframework.boot.test.autoconfigure.web.client;

import java.lang.Override;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.client.RestTemplate;
import slim.ConditionService;

public class WebClientRestTemplateAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(WebClientRestTemplateAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(WebClientRestTemplateAutoConfiguration.class).length==0) {
        context.registerBean(WebClientRestTemplateAutoConfiguration.class, () -> new WebClientRestTemplateAutoConfiguration());
        context.registerBean("restTemplate", RestTemplate.class, () -> context.getBean(WebClientRestTemplateAutoConfiguration.class).restTemplate(context.getBean(RestTemplateBuilder.class)));
      }
    }
  }
}
