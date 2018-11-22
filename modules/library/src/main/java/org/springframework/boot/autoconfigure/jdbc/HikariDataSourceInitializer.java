package org.springframework.boot.autoconfigure.jdbc;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import slim.ConditionService;

public class HikariDataSourceInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {
	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = context.getBeanFactory()
				.getBean(ConditionService.class);
		if (conditions.matches(DataSourceConfiguration.Hikari.class)) {
			context.registerBean(DataSourceConfiguration.Hikari.class,
					() -> new DataSourceConfiguration.Hikari());
			context.registerBean("dataSource", DataSource.class,
					() -> context.getBean(DataSourceConfiguration.Hikari.class)
							.dataSource(context.getBean(DataSourceProperties.class)));
		}
	}

	public static Class<?> configurations() {
		return DataSourceJmxConfiguration.Hikari.class;
	}
}