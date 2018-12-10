package org.springframework.boot.autoconfigure.integration;

import java.lang.Override;
import javax.sql.DataSource;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ResourceLoader;
import slim.ConditionService;

public class IntegrationAutoConfiguration_IntegrationJdbcConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(IntegrationAutoConfiguration.IntegrationJdbcConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(IntegrationAutoConfiguration.IntegrationJdbcConfiguration.class).length==0) {
        context.registerBean(IntegrationAutoConfiguration.IntegrationJdbcConfiguration.class, () -> new IntegrationAutoConfiguration.IntegrationJdbcConfiguration());
        if (conditions.matches(IntegrationAutoConfiguration.IntegrationJdbcConfiguration.class, IntegrationDataSourceInitializer.class)) {
          context.registerBean("integrationDataSourceInitializer", IntegrationDataSourceInitializer.class, () -> context.getBean(IntegrationAutoConfiguration.IntegrationJdbcConfiguration.class).integrationDataSourceInitializer(context.getBean(DataSource.class),context.getBean(ResourceLoader.class),context.getBean(IntegrationProperties.class)));
        }
      }
    }
  }
}
