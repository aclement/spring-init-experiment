package org.springframework.boot.autoconfigure.jackson;

import java.lang.Override;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class JacksonAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JacksonAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(JacksonAutoConfiguration.class).length==0) {
        new JacksonAutoConfiguration_JacksonObjectMapperConfigurationInitializer().initialize(context);
        new JacksonAutoConfiguration_JodaDateTimeJacksonConfigurationInitializer().initialize(context);
        new JacksonAutoConfiguration_ParameterNamesModuleConfigurationInitializer().initialize(context);
        new JacksonAutoConfiguration_JacksonObjectMapperBuilderConfigurationInitializer().initialize(context);
        new JacksonAutoConfiguration_Jackson2ObjectMapperBuilderCustomizerConfigurationInitializer().initialize(context);
        context.registerBean(JacksonAutoConfiguration.class, () -> new JacksonAutoConfiguration());
        context.registerBean("jsonComponentModule", JsonComponentModule.class, () -> context.getBean(JacksonAutoConfiguration.class).jsonComponentModule());
      }
    }
  }
}
