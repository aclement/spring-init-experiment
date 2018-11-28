package org.springframework.boot.autoconfigure.data.jdbc;

import java.util.Optional;

import slim.ConditionService;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.JdbcConfiguration;
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
						JdbcRepositoriesAutoConfiguration_SpringBootJdbcConfigurationInitializer.SpringBootJdbcConfiguration.class,
						() -> new JdbcRepositoriesAutoConfiguration_SpringBootJdbcConfigurationInitializer.SpringBootJdbcConfiguration());
			}
			context.registerBean("jdbcCustomConversions", JdbcCustomConversions.class,
					() -> context.getBean(
							JdbcRepositoriesAutoConfiguration_SpringBootJdbcConfigurationInitializer.SpringBootJdbcConfiguration.class)
							.jdbcCustomConversions());
			context.registerBean("jdbcMappingContext", RelationalMappingContext.class,
					() -> context.getBean(
							JdbcRepositoriesAutoConfiguration_SpringBootJdbcConfigurationInitializer.SpringBootJdbcConfiguration.class)
							.jdbcMappingContext(Optional.ofNullable(
									context.getBeanProvider(NamingStrategy.class)
											.getIfAvailable())));
			context.registerBean("relationalConverter", RelationalConverter.class,
					() -> context.getBean(
							JdbcRepositoriesAutoConfiguration_SpringBootJdbcConfigurationInitializer.SpringBootJdbcConfiguration.class)
							.relationalConverter(
									context.getBean(RelationalMappingContext.class)));
		}
	}

	protected class SpringBootJdbcConfiguration extends JdbcConfiguration {

		@Override
		protected RelationalMappingContext jdbcMappingContext(
				Optional<NamingStrategy> namingStrategy) {
			return super.jdbcMappingContext(namingStrategy);
		}

		@Override
		protected RelationalConverter relationalConverter(
				RelationalMappingContext mappingContext) {
			return super.relationalConverter(mappingContext);
		}

		@Override
		protected JdbcCustomConversions jdbcCustomConversions() {
			return super.jdbcCustomConversions();
		}

	}
}