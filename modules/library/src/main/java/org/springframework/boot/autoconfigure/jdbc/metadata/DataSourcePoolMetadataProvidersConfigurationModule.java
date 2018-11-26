package org.springframework.boot.autoconfigure.jdbc.metadata;

import java.util.Arrays;
import java.util.List;

import slim.Module;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class DataSourcePoolMetadataProvidersConfigurationModule implements Module {
	@Override
	public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
		return Arrays
				.asList(new DataSourcePoolMetadataProvidersConfigurationInitializer());
	}

	@Override
	public Class<?> getRoot() {
		return DataSourcePoolMetadataProvidersConfiguration.class;
	}
}
