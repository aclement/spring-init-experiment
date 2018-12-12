package org.springframework.boot.test.autoconfigure.json;

import com.google.gson.Gson;

import slim.ConditionService;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class JsonTestersAutoConfiguration_GsonJsonTestersConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {
	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = (ConditionService) context.getBeanFactory()
				.getSingleton(ConditionService.class.getName());
		if (conditions.matches(
				JsonTestersAutoConfiguration.GsonJsonTestersConfiguration.class)) {
			if (context.getBeanFactory().getBeanNamesForType(
					JsonTestersAutoConfiguration.GsonJsonTestersConfiguration.class).length == 0) {
				context.registerBean(
						JsonTestersAutoConfiguration.GsonJsonTestersConfiguration.class,
						() -> new JsonTestersAutoConfiguration.GsonJsonTestersConfiguration());
				if (conditions.matches(
						JsonTestersAutoConfiguration.GsonJsonTestersConfiguration.class,
						FactoryBean.class)) {
					context.registerBean("gsonTesterFactoryBean", FactoryBean.class,
							() -> context.getBean(
									JsonTestersAutoConfiguration.GsonJsonTestersConfiguration.class)
									.gsonTesterFactoryBean(context.getBean(Gson.class)),
							def -> {
								def.setFactoryBeanName(
										JsonTestersAutoConfiguration.GsonJsonTestersConfiguration.class
												.getName());
								def.setFactoryMethodName("gsonTesterFactoryBean");
							});
				}
			}
		}
	}
}
