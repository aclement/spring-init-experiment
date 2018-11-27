/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.jackson;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.Jackson2ObjectMapperBuilderCustomizerConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.JacksonObjectMapperBuilderConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.JacksonObjectMapperConfiguration;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import slim.ConditionService;

/**
 * @author Dave Syer
 *
 */
public class JacksonAutoConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {

	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = context.getBeanFactory()
				.getBean(ConditionService.class);
		if (conditions.matches(JacksonAutoConfiguration.class)) {
			context.registerBean(JacksonProperties.class, () -> new JacksonProperties());
			context.registerBean(JacksonAutoConfiguration.class);
			context.registerBean(JsonComponentModule.class, () -> context
					.getBean(JacksonAutoConfiguration.class).jsonComponentModule());
			// TODO: copy more code from package private nested classes
			if (conditions.matches(JacksonObjectMapperBuilderConfiguration.class)) {
				context.registerBean(JacksonObjectMapperBuilderConfiguration.class);
				if (conditions.matches(JacksonObjectMapperBuilderConfiguration.class,
						Jackson2ObjectMapperBuilder.class)) {
					context.registerBean(Jackson2ObjectMapperBuilder.class, () -> context
							.getBean(JacksonObjectMapperBuilderConfiguration.class)
							.jacksonObjectMapperBuilder(new ArrayList<>(context
									.getBeansOfType(
											Jackson2ObjectMapperBuilderCustomizer.class)
									.values())));
				}
			}
			if (conditions.matches(JacksonObjectMapperConfiguration.class)) {
				context.registerBean(JacksonObjectMapperConfiguration.class);
				context.registerBean("jacksonObjectMapper", ObjectMapper.class,
						() -> context.getBean(JacksonObjectMapperConfiguration.class)
								.jacksonObjectMapper(context
										.getBean(Jackson2ObjectMapperBuilder.class)));
			}
			if (conditions
					.matches(Jackson2ObjectMapperBuilderCustomizerConfiguration.class)) {
				context.registerBean(
						Jackson2ObjectMapperBuilderCustomizerConfiguration.class);
				context.registerBean("standardJackson2ObjectMapperBuilderCustomizer",
						Jackson2ObjectMapperBuilderCustomizer.class,
						() -> context.getBean(
								Jackson2ObjectMapperBuilderCustomizerConfiguration.class)
								.standardJacksonObjectMapperBuilderCustomizer(context,
										context.getBean(JacksonProperties.class)));
			}
		}
	}

}
