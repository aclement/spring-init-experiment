package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Class;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import slim.ConditionService;
import slim.ModuleMapping;

@ModuleMapping(
    module = DataSourceAutoConfigurationModule.class
)
public class JdbcTemplateAutoConfiguration_NamedParameterJdbcTemplateConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    new JdbcTemplateAutoConfiguration_JdbcTemplateConfigurationInitializer().initialize(context);
    context.registerBean(JdbcTemplateAutoConfiguration.NamedParameterJdbcTemplateConfiguration.class, () -> new JdbcTemplateAutoConfiguration.NamedParameterJdbcTemplateConfiguration());
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JdbcTemplateAutoConfiguration.NamedParameterJdbcTemplateConfiguration.class, NamedParameterJdbcTemplate.class)) {
      context.registerBean("namedParameterJdbcTemplate", NamedParameterJdbcTemplate.class, () -> context.getBean(JdbcTemplateAutoConfiguration.NamedParameterJdbcTemplateConfiguration.class).namedParameterJdbcTemplate(context.getBean(JdbcTemplate.class)));
    }
  }

  public static Class<?> configurations() {
    return JdbcTemplateAutoConfiguration.NamedParameterJdbcTemplateConfiguration.class;
  }
}
