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

package org.springframework.boot.autoconfigure.gson;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import slim.ConditionService;

/**
 * @author Dave Syer
 *
 */
public class GsonAutoConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {

	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = context.getBeanFactory()
				.getBean(ConditionService.class);
		if (conditions.matches(GsonAutoConfiguration.class)) {
			context.registerBean(GsonProperties.class, () -> new GsonProperties());
			context.registerBean(GsonAutoConfiguration.class);
			if (conditions.matches(GsonAutoConfiguration.class, GsonBuilder.class)) {
				context.registerBean("gsonBuilder", GsonBuilder.class,
						() -> context.getBean(GsonAutoConfiguration.class)
								.gsonBuilder(new ArrayList<>(context
										.getBeansOfType(GsonBuilderCustomizer.class)
										.values())));
			}
			if (conditions.matches(GsonAutoConfiguration.class, Gson.class)) {
				context.registerBean("gson", Gson.class,
						() -> context.getBean(GsonAutoConfiguration.class)
								.gson(context.getBean(GsonBuilder.class)));
			}
			context.registerBean("standardGsonBuilderCustomizer",
					GsonBuilderCustomizer.class,
					() -> context.getBean(GsonAutoConfiguration.class)
							.standardGsonBuilderCustomizer(
									context.getBean(GsonProperties.class)));
		}
	}

}
