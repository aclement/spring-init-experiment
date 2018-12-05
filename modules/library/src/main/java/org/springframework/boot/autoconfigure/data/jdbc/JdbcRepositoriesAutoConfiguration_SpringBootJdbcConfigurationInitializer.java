package org.springframework.boot.autoconfigure.data.jdbc;

import java.util.Optional;

import slim.ConditionService;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.relational.core.conversion.RelationalConverter;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;

public class JdbcRepositoriesAutoConfiguration_SpringBootJdbcConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {
	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = context.getBeanFactory()
				.getBean(ConditionService.class);
		if (conditions.matches(
				JdbcRepositoriesAutoConfiguration.SpringBootJdbcConfiguration.class)) {
			if (context.getBeanFactory().getBeanNamesForType(
					JdbcRepositoriesAutoConfiguration.SpringBootJdbcConfiguration.class).length == 0) {
				context.registerBean(
						JdbcRepositoriesAutoConfiguration.SpringBootJdbcConfiguration.class,
						() -> new JdbcRepositoriesAutoConfiguration.SpringBootJdbcConfiguration());
				context.registerBean("jdbcCustomConversions", JdbcCustomConversions.class,
						() -> context.getBean(JdbcRepositoriesAutoConfiguration.SpringBootJdbcConfiguration.class)
								.jdbcCustomConversions());
				context.registerBean("jdbcMappingContext", RelationalMappingContext.class,
						() -> context.getBean(JdbcRepositoriesAutoConfiguration.SpringBootJdbcConfiguration.class)
								.jdbcMappingContext(Optional.ofNullable(
										context.getBeanProvider(NamingStrategy.class)
												.getIfAvailable())));
				context.registerBean("relationalConverter", RelationalConverter.class,
						() -> context.getBean(JdbcRepositoriesAutoConfiguration.SpringBootJdbcConfiguration.class)
								.relationalConverter(
										context.getBean(RelationalMappingContext.class)));
			}
		}
	}

}
