package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Class;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class EmbeddedDatabaseConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {
	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = context.getBeanFactory()
				.getBean(ConditionService.class);
		if (conditions.matches(
				DataSourceAutoConfiguration.EmbeddedDatabaseConfiguration.class)) {
			new EmbeddedDataSourceConfigurationInitializer().initialize(context);
		}
	}

	public static Class<?> configurations() {
		return DataSourceAutoConfiguration.EmbeddedDatabaseConfiguration.class;
	}
}