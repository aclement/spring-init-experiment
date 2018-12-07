package org.springframework.boot.actuate.autoconfigure.info;

import java.lang.Override;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class InfoEndpointAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(InfoEndpointAutoConfiguration.class).length==0) {
      context.registerBean(InfoEndpointAutoConfiguration.class, () -> new InfoEndpointAutoConfiguration());
      ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
      if (conditions.matches(InfoEndpointAutoConfiguration.class, InfoEndpoint.class)) {
        context.registerBean("infoEndpoint", InfoEndpoint.class, () -> context.getBean(InfoEndpointAutoConfiguration.class).infoEndpoint(context.getBeanProvider(InfoContributor.class)));
      }
    }
  }
}
