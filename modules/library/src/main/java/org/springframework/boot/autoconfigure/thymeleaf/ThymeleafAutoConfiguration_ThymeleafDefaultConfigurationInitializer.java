package org.springframework.boot.autoconfigure.thymeleaf;

import java.util.stream.Collectors;

import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;

import slim.ConditionService;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class ThymeleafAutoConfiguration_ThymeleafDefaultConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {
	@Override
	public void initialize(GenericApplicationContext context) {
		if (context.getBeanFactory().getBeanNamesForType(
				ThymeleafAutoConfiguration.ThymeleafDefaultConfiguration.class).length == 0) {
			context.registerBean(
					ThymeleafAutoConfiguration.ThymeleafDefaultConfiguration.class,
					() -> new ThymeleafAutoConfiguration.ThymeleafDefaultConfiguration(
							context.getBean(ThymeleafProperties.class),
							context.getBeanProvider(ITemplateResolver.class).stream()
									.collect(Collectors.toSet()),
							context.getBeanProvider(IDialect.class)));
			ConditionService conditions = context.getBeanFactory()
					.getBean(ConditionService.class);
			if (conditions.matches(
					ThymeleafAutoConfiguration.ThymeleafDefaultConfiguration.class,
					SpringTemplateEngine.class)) {
				context.registerBean("templateEngine", SpringTemplateEngine.class,
						() -> context.getBean(
								ThymeleafAutoConfiguration.ThymeleafDefaultConfiguration.class)
								.templateEngine());
			}
		}
	}
}
