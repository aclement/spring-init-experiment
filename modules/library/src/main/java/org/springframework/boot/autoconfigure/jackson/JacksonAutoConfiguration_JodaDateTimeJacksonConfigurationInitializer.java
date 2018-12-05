package org.springframework.boot.autoconfigure.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class JacksonAutoConfiguration_JodaDateTimeJacksonConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JacksonAutoConfiguration.JodaDateTimeJacksonConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(JacksonAutoConfiguration.JodaDateTimeJacksonConfiguration.class).length==0) {
        context.registerBean(JacksonAutoConfiguration.JodaDateTimeJacksonConfiguration.class, () -> new JacksonAutoConfiguration.JodaDateTimeJacksonConfiguration(context.getBean(JacksonProperties.class)));
        context.registerBean("jodaDateTimeSerializationModule", SimpleModule.class, () -> context.getBean(JacksonAutoConfiguration.JodaDateTimeJacksonConfiguration.class).jodaDateTimeSerializationModule());
      }
    }
  }
}
