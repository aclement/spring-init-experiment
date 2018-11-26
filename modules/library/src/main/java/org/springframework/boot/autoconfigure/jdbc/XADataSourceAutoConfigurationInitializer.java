package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Class;
import java.lang.Override;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ModuleMapping;

@ModuleMapping(
    module = DataSourceAutoConfigurationModule.class
)
public class XADataSourceAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(XADataSourceAutoConfiguration.class)) {
      context.registerBean(DataSourceProperties.class, () -> new DataSourceProperties());
      context.registerBean(XADataSourceAutoConfiguration.class, () -> new XADataSourceAutoConfiguration(context.getBean(XADataSourceWrapper.class),context.getBean(DataSourceProperties.class),context.getBeanProvider(XADataSource.class)));
      context.registerBean("dataSource", DataSource.class, () -> { try { return context.getBean(XADataSourceAutoConfiguration.class).dataSource(); } catch (Exception e) { throw new IllegalStateException(e); } });
    }
  }

  public static Class<?> configurations() {
    return XADataSourceAutoConfiguration.class;
  }
}
