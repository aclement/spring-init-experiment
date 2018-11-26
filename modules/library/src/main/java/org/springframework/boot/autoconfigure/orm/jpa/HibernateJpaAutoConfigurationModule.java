package org.springframework.boot.autoconfigure.orm.jpa;

import java.util.Arrays;
import java.util.List;

import slim.Module;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class HibernateJpaAutoConfigurationModule implements Module {
	@Override
	public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
		return Arrays.asList(new HibernateJpaAutoConfigurationInitializer());
	}

	@Override
	public Class<?> getRoot() {
		return HibernateJpaAutoConfiguration.class;
	}
}
