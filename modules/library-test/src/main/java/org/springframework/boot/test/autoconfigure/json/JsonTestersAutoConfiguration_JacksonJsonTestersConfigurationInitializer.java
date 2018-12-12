package org.springframework.boot.test.autoconfigure.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import slim.ConditionService;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class JsonTestersAutoConfiguration_JacksonJsonTestersConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {
	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = (ConditionService) context.getBeanFactory()
				.getSingleton(ConditionService.class.getName());
		if (conditions.matches(
				JsonTestersAutoConfiguration.JacksonJsonTestersConfiguration.class)) {
			if (context.getBeanFactory().getBeanNamesForType(
					JsonTestersAutoConfiguration.JacksonJsonTestersConfiguration.class).length == 0) {
				context.registerBean(
						JsonTestersAutoConfiguration.JacksonJsonTestersConfiguration.class,
						() -> new JsonTestersAutoConfiguration.JacksonJsonTestersConfiguration());
				if (conditions.matches(
						JsonTestersAutoConfiguration.JacksonJsonTestersConfiguration.class,
						FactoryBean.class)) {
					context.registerBean("jacksonTesterFactoryBean", FactoryBean.class,
							() -> context.getBean(
									JsonTestersAutoConfiguration.JacksonJsonTestersConfiguration.class)
									.jacksonTesterFactoryBean(
											context.getBean(ObjectMapper.class)),
							def -> {
								def.setFactoryBeanName(
										JsonTestersAutoConfiguration.JacksonJsonTestersConfiguration.class
												.getName());
								def.setFactoryMethodName("jacksonTesterFactoryBean");
							});
				}
			}
		}
	}
}
