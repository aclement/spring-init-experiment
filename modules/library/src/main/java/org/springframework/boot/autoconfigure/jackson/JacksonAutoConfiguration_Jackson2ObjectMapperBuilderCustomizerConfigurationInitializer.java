package org.springframework.boot.autoconfigure.jackson;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class JacksonAutoConfiguration_Jackson2ObjectMapperBuilderCustomizerConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JacksonAutoConfiguration.Jackson2ObjectMapperBuilderCustomizerConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(JacksonAutoConfiguration.Jackson2ObjectMapperBuilderCustomizerConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(JacksonAutoConfiguration.Jackson2ObjectMapperBuilderCustomizerConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(JacksonAutoConfiguration.Jackson2ObjectMapperBuilderCustomizerConfiguration.class, () -> new JacksonAutoConfiguration.Jackson2ObjectMapperBuilderCustomizerConfiguration());
        context.registerBean("standardJacksonObjectMapperBuilderCustomizer", JacksonAutoConfiguration.Jackson2ObjectMapperBuilderCustomizerConfiguration.StandardJackson2ObjectMapperBuilderCustomizer.class, () -> context.getBean(JacksonAutoConfiguration.Jackson2ObjectMapperBuilderCustomizerConfiguration.class).standardJacksonObjectMapperBuilderCustomizer(context,context.getBean(JacksonProperties.class)));
      }
    }
  }
}
