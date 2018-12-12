package org.springframework.boot.test.autoconfigure.jdbc;

import java.lang.Override;
import javax.sql.DataSource;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import slim.ConditionService;

public class TestDatabaseAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(TestDatabaseAutoConfiguration.class).length==0) {
      context.registerBean(TestDatabaseAutoConfiguration.class, () -> new TestDatabaseAutoConfiguration(context.getBean(Environment.class)));
      ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
      if (conditions.matches(TestDatabaseAutoConfiguration.class, DataSource.class)) {
        context.registerBean("dataSource", DataSource.class, () -> context.getBean(TestDatabaseAutoConfiguration.class).dataSource());
      }
      if (conditions.matches(TestDatabaseAutoConfiguration.class, BeanDefinitionRegistryPostProcessor.class)) {
        context.registerBean("embeddedDataSourceBeanFactoryPostProcessor", BeanDefinitionRegistryPostProcessor.class, () -> context.getBean(TestDatabaseAutoConfiguration.class).embeddedDataSourceBeanFactoryPostProcessor());
      }
    }
  }
}
