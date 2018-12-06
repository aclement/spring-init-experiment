package org.springframework.boot.autoconfigure.context;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.MessageSource;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class MessageSourceAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MessageSourceAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(MessageSourceAutoConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(MessageSourceAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(MessageSourceAutoConfiguration.class, () -> new MessageSourceAutoConfiguration());
        context.registerBean("messageSourceProperties", MessageSourceProperties.class, () -> context.getBean(MessageSourceAutoConfiguration.class).messageSourceProperties());
        context.registerBean("messageSource", MessageSource.class, () -> context.getBean(MessageSourceAutoConfiguration.class).messageSource(context.getBean(MessageSourceProperties.class)));
      }
    }
  }
}
