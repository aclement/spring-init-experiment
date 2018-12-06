package org.springframework.boot.autoconfigure.thymeleaf;

import com.github.mxab.thymeleaf.extras.dataattribute.dialect.DataAttributeDialect;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ThymeleafAutoConfiguration_DataAttributeDialectConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ThymeleafAutoConfiguration.DataAttributeDialectConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ThymeleafAutoConfiguration.DataAttributeDialectConfiguration.class).length==0) {
        context.registerBean(ThymeleafAutoConfiguration.DataAttributeDialectConfiguration.class, () -> new ThymeleafAutoConfiguration.DataAttributeDialectConfiguration());
        if (conditions.matches(ThymeleafAutoConfiguration.DataAttributeDialectConfiguration.class, DataAttributeDialect.class)) {
          context.registerBean("dialect", DataAttributeDialect.class, () -> context.getBean(ThymeleafAutoConfiguration.DataAttributeDialectConfiguration.class).dialect());
        }
      }
    }
  }
}
