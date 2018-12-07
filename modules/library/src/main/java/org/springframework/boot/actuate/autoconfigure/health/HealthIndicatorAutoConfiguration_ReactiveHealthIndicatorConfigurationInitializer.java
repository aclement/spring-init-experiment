package org.springframework.boot.actuate.autoconfigure.health;

import java.util.Map;

import slim.ConditionService;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.actuate.health.ReactiveHealthIndicatorRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ReflectionUtils;

public class HealthIndicatorAutoConfiguration_ReactiveHealthIndicatorConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {

	private DependencyDescriptor reactiveHealthIndicators;
	private DependencyDescriptor healthIndicators;

	public HealthIndicatorAutoConfiguration_ReactiveHealthIndicatorConfigurationInitializer() {
		reactiveHealthIndicators = new DependencyDescriptor(ReflectionUtils.findField(
				HealthIndicatorAutoConfiguration_ReactiveHealthIndicatorConfigurationInitializer.class,
				"reactiveHealthIndicatorsProvider"), true);
		healthIndicators = new DependencyDescriptor(ReflectionUtils.findField(
				HealthIndicatorAutoConfiguration_ReactiveHealthIndicatorConfigurationInitializer.class,
				"healthIndicatorsProvider"), true);
	}

	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = context.getBeanFactory()
				.getBean(ConditionService.class);
		if (conditions.matches(
				HealthIndicatorAutoConfiguration.ReactiveHealthIndicatorConfiguration.class)) {
			if (context.getBeanFactory().getBeanNamesForType(
					HealthIndicatorAutoConfiguration.ReactiveHealthIndicatorConfiguration.class).length == 0) {
				context.registerBean(
						HealthIndicatorAutoConfiguration.ReactiveHealthIndicatorConfiguration.class,
						() -> new HealthIndicatorAutoConfiguration.ReactiveHealthIndicatorConfiguration());
				if (conditions.matches(
						HealthIndicatorAutoConfiguration.ReactiveHealthIndicatorConfiguration.class,
						ReactiveHealthIndicatorRegistry.class)) {
					context.registerBean("reactiveHealthIndicatorRegistry",
							ReactiveHealthIndicatorRegistry.class,
							() -> context.getBean(
									HealthIndicatorAutoConfiguration.ReactiveHealthIndicatorConfiguration.class)
									.reactiveHealthIndicatorRegistry(
											(ObjectProvider<Map<String, ReactiveHealthIndicator>>) context
													.getBeanFactory().resolveDependency(
															reactiveHealthIndicators,
															"config"),
											(ObjectProvider<Map<String, HealthIndicator>>) context
													.getBeanFactory().resolveDependency(
															healthIndicators, "config")));
				}
			}
		}
	}

	private ObjectProvider<Map<String, HealthIndicator>> healthIndicatorsProvider;
	private ObjectProvider<Map<String, ReactiveHealthIndicator>> reactiveHealthIndicatorsProvider;
}
