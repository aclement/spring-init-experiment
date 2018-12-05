package org.springframework.boot.autoconfigure.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import slim.ConditionService;

public class JacksonAutoConfiguration_JacksonObjectMapperConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JacksonAutoConfiguration.JacksonObjectMapperConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(JacksonAutoConfiguration.JacksonObjectMapperConfiguration.class).length==0) {
        context.registerBean(JacksonAutoConfiguration.JacksonObjectMapperConfiguration.class, () -> new JacksonAutoConfiguration.JacksonObjectMapperConfiguration());
        if (conditions.matches(JacksonAutoConfiguration.JacksonObjectMapperConfiguration.class, ObjectMapper.class)) {
          context.registerBean("jacksonObjectMapper", ObjectMapper.class, () -> context.getBean(JacksonAutoConfiguration.JacksonObjectMapperConfiguration.class).jacksonObjectMapper(context.getBean(Jackson2ObjectMapperBuilder.class)));
        }
      }
    }
  }
}
