package org.springframework.boot.actuate.autoconfigure.endpoint;

import java.lang.Override;
import org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper;
import org.springframework.boot.actuate.endpoint.invoker.cache.CachingOperationInvokerAdvisor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import slim.ConditionService;

public class EndpointAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(EndpointAutoConfiguration.class).length==0) {
      context.registerBean(EndpointAutoConfiguration.class, () -> new EndpointAutoConfiguration());
      ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
      if (conditions.matches(EndpointAutoConfiguration.class, ParameterValueMapper.class)) {
        context.registerBean("endpointOperationParameterMapper", ParameterValueMapper.class, () -> context.getBean(EndpointAutoConfiguration.class).endpointOperationParameterMapper());
      }
      if (conditions.matches(EndpointAutoConfiguration.class, CachingOperationInvokerAdvisor.class)) {
        context.registerBean("endpointCachingOperationInvokerAdvisor", CachingOperationInvokerAdvisor.class, () -> context.getBean(EndpointAutoConfiguration.class).endpointCachingOperationInvokerAdvisor(context.getBean(Environment.class)));
      }
    }
  }
}
