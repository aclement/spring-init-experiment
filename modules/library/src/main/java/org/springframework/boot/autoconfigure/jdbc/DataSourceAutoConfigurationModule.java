package org.springframework.boot.autoconfigure.jdbc;

import java.lang.Override;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvidersConfigurationModule;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import slim.Module;

@Import({ DataSourcePoolMetadataProvidersConfigurationModule.class, DataSourceAutoConfiguration.EmbeddedDatabaseConfiguration.class,
		DataSourceAutoConfiguration.PooledDataSourceConfiguration.class,
		DataSourceInitializationConfiguration.class, DataSourceJmxConfiguration.class,
		DataSourceJmxConfiguration.Hikari.class,
		DataSourceConfiguration.Hikari.class,
		DataSourceJmxConfiguration.TomcatDataSourceJmxConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.DataSourceTransactionManagerConfiguration.class,
		EmbeddedDataSourceConfiguration.class, JdbcTemplateAutoConfiguration.class,
		JdbcTemplateAutoConfiguration.JdbcTemplateConfiguration.class,
		JdbcTemplateAutoConfiguration.NamedParameterJdbcTemplateConfiguration.class,
		JndiDataSourceAutoConfiguration.class, XADataSourceAutoConfiguration.class })
public class DataSourceAutoConfigurationModule implements Module {
	@Override
	public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
		return Arrays.asList(new JdbcTemplateConfigurationInitializer(),
				new DataSourceInitializationConfigurationInitializer(),
				new DataSourceAutoConfigurationInitializer(),
				new DataSourceTransactionManagerConfigurationInitializer(),
				new JndiDataSourceAutoConfigurationInitializer(),
				new TomcatDataSourceJmxConfigurationInitializer(),
				new DataSourceJmxConfigurationInitializer(),
				new DataSourceTransactionManagerAutoConfigurationInitializer(),
				new HikariJmxInitializer(), 
				new HikariDataSourceInitializer(), 
				new EmbeddedDatabaseConfigurationInitializer(),
				new EmbeddedDatabaseConfigurationInitializer(),
				new NamedParameterJdbcTemplateConfigurationInitializer(),
				new PooledDataSourceConfigurationInitializer(),
				new XADataSourceAutoConfigurationInitializer(),
				new JdbcTemplateAutoConfigurationInitializer());
	}
}
