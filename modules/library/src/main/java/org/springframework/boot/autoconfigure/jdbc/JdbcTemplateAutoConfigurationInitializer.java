package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class JdbcTemplateAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JdbcTemplateAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(JdbcTemplateAutoConfiguration.class).length==0) {
        new JdbcTemplateAutoConfiguration_JdbcTemplateConfigurationInitializer().initialize(context);
        new JdbcTemplateAutoConfiguration_NamedParameterJdbcTemplateConfigurationInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(JdbcTemplateAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(JdbcTemplateAutoConfiguration.class, () -> new JdbcTemplateAutoConfiguration());
      }
    }
  }
}
