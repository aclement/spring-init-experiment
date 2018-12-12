package org.springframework.boot.test.autoconfigure.json;

import java.lang.reflect.Field;

import slim.ConditionService;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.boot.test.json.AbstractJsonMarshalTester;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

public class JsonTestersAutoConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {
	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = (ConditionService) context.getBeanFactory()
				.getSingleton(ConditionService.class.getName());
		if (conditions.matches(JsonTestersAutoConfiguration.class)) {
			if (context.getBeanFactory().getBeanNamesForType(
					JsonTestersAutoConfiguration.class).length == 0) {
				new JsonTestersAutoConfiguration_JacksonJsonTestersConfigurationInitializer()
						.initialize(context);
				new JsonTestersAutoConfiguration_GsonJsonTestersConfigurationInitializer()
						.initialize(context);
				new JsonTestersAutoConfiguration_JsonbJsonTesterConfigurationInitializer()
						.initialize(context);
				context.registerBean(JsonTestersAutoConfiguration.class,
						() -> new JsonTestersAutoConfiguration());
				context.registerBean(JsonMarshalTestersBeanPostProcessor.class,
						() -> new JsonMarshalTestersBeanPostProcessor());
				context.registerBean("basicJsonTesterFactoryBean", FactoryBean.class,
						() -> context.getBean(JsonTestersAutoConfiguration.class)
								.basicJsonTesterFactoryBean(),
						def -> {
							def.setFactoryBeanName(
									JsonTestersAutoConfiguration.class.getName());
							def.setFactoryMethodName("basicJsonTesterFactoryBean");
						});
			}
		}
	}

	static class JsonMarshalTestersBeanPostProcessor
			extends InstantiationAwareBeanPostProcessorAdapter {

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName)
				throws BeansException {
			ReflectionUtils.doWithFields(bean.getClass(),
					(field) -> processField(bean, field));
			return bean;
		}

		private void processField(Object bean, Field field) {
			if (AbstractJsonMarshalTester.class.isAssignableFrom(field.getType())) {
				initializeTester(bean, field, bean.getClass(),
						ResolvableType.forField(field).getGeneric());
			}
			else if (BasicJsonTester.class.isAssignableFrom(field.getType())) {
				initializeTester(bean, field, bean.getClass());
			}
		}

		private void initializeTester(Object bean, Field field, Object... args) {
			ReflectionUtils.makeAccessible(field);
			Object tester = ReflectionUtils.getField(field, bean);
			if (tester != null) {
				ReflectionTestUtils.invokeMethod(tester, "initialize", args);
			}
		}

	}
}
