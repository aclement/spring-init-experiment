package org.springframework.boot.autoconfigure.jdbc;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import slim.ConditionService;

public class JdbcTemplateAutoConfiguration_JdbcTemplateConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    context.registerBean(JdbcTemplateAutoConfiguration.JdbcTemplateConfiguration.class, () -> new JdbcTemplateAutoConfiguration.JdbcTemplateConfiguration(context.getBean(DataSource.class),context.getBean(JdbcProperties.class)));
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(JdbcTemplateAutoConfiguration.JdbcTemplateConfiguration.class, JdbcTemplate.class)) {
      context.registerBean("jdbcTemplate", JdbcTemplate.class, () -> context.getBean(JdbcTemplateAutoConfiguration.JdbcTemplateConfiguration.class).jdbcTemplate());
    }
  }

  public static Class<?> configurations() {
    return JdbcTemplateAutoConfiguration.JdbcTemplateConfiguration.class;
  }
}
