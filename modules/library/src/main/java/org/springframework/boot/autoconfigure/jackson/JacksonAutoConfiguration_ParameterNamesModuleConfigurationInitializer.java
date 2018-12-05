package org.springframework.boot.autoconfigure.jackson;

import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class JacksonAutoConfiguration_ParameterNamesModuleConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JacksonAutoConfiguration.ParameterNamesModuleConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(JacksonAutoConfiguration.ParameterNamesModuleConfiguration.class).length==0) {
        context.registerBean(JacksonAutoConfiguration.ParameterNamesModuleConfiguration.class, () -> new JacksonAutoConfiguration.ParameterNamesModuleConfiguration());
        if (conditions.matches(JacksonAutoConfiguration.ParameterNamesModuleConfiguration.class, ParameterNamesModule.class)) {
          context.registerBean("parameterNamesModule", ParameterNamesModule.class, () -> context.getBean(JacksonAutoConfiguration.ParameterNamesModuleConfiguration.class).parameterNamesModule());
        }
      }
    }
  }
}
