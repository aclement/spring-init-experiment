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

package org.springframework.boot.autoconfigure.web.reactive;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import slim.ConditionService;

/**
 * @author Dave Syer
 *
 */
public class ReactiveWebServerFactoryAutoConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {
	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = context.getBeanFactory()
				.getBean(ConditionService.class);
		if (conditions.matches(ReactiveWebServerFactoryAutoConfiguration.class)) {
			WebFluxAutoConfigurationGenerated.initializer().initialize(context);
			HttpHandlerAutoConfigurationGenerated.initializer().initialize(context);
			ErrorWebFluxAutoConfigurationGenerated.initializer().initialize(context);
			// This one has to jump the queue. TODO: try something different
			ReactiveWebServerFactoryAutoConfigurationGenerated.initializer()
					.initialize(context);
			context.registerBean(WebFluxProperties.class, () -> new WebFluxProperties());
		}
	}

}
