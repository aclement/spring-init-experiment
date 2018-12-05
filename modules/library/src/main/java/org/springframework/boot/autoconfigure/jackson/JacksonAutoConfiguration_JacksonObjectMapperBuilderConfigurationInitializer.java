package org.springframework.boot.autoconfigure.jackson;

import java.lang.Override;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import slim.ConditionService;

public class JacksonAutoConfiguration_JacksonObjectMapperBuilderConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JacksonAutoConfiguration.JacksonObjectMapperBuilderConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(JacksonAutoConfiguration.JacksonObjectMapperBuilderConfiguration.class).length==0) {
        context.registerBean(JacksonAutoConfiguration.JacksonObjectMapperBuilderConfiguration.class, () -> new JacksonAutoConfiguration.JacksonObjectMapperBuilderConfiguration(context));
        if (conditions.matches(JacksonAutoConfiguration.JacksonObjectMapperBuilderConfiguration.class, Jackson2ObjectMapperBuilder.class)) {
          context.registerBean("jacksonObjectMapperBuilder", Jackson2ObjectMapperBuilder.class, () -> context.getBean(JacksonAutoConfiguration.JacksonObjectMapperBuilderConfiguration.class).jacksonObjectMapperBuilder(context.getBeanProvider(Jackson2ObjectMapperBuilderCustomizer.class).stream().collect(Collectors.toList())));
        }
      }
    }
  }
}
