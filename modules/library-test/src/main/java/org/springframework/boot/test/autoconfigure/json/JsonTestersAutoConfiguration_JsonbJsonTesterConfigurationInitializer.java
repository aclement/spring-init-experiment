package org.springframework.boot.test.autoconfigure.json;

import javax.json.bind.Jsonb;

import slim.ConditionService;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class JsonTestersAutoConfiguration_JsonbJsonTesterConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {
	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = (ConditionService) context.getBeanFactory()
				.getSingleton(ConditionService.class.getName());
		if (conditions.matches(
				JsonTestersAutoConfiguration.JsonbJsonTesterConfiguration.class)) {
			if (context.getBeanFactory().getBeanNamesForType(
					JsonTestersAutoConfiguration.JsonbJsonTesterConfiguration.class).length == 0) {
				context.registerBean(
						JsonTestersAutoConfiguration.JsonbJsonTesterConfiguration.class,
						() -> new JsonTestersAutoConfiguration.JsonbJsonTesterConfiguration());
				if (conditions.matches(
						JsonTestersAutoConfiguration.JsonbJsonTesterConfiguration.class,
						FactoryBean.class)) {
					context.registerBean("jsonbTesterFactoryBean", FactoryBean.class,
							() -> context.getBean(
									JsonTestersAutoConfiguration.JsonbJsonTesterConfiguration.class)
									.jsonbTesterFactoryBean(context.getBean(Jsonb.class)),
							def -> {
								def.setFactoryBeanName(
										JsonTestersAutoConfiguration.JsonbJsonTesterConfiguration.class
												.getName());
								def.setFactoryMethodName("jsonbTesterFactoryBean");
							});
				}
			}
		}
	}
}
