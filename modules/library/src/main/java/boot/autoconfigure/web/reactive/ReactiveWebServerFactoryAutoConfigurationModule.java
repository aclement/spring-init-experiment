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

package boot.autoconfigure.web.reactive;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import boot.autoconfigure.http.HttpMessageConvertersAutoConfigurationModule;
import boot.autoconfigure.reactor.ReactorCoreAutoConfigurationModule;
import slim.ConditionService;
import slim.Module;
import slim.InitializerMapping;
import slim.SlimConfiguration;

/**
 * @author Dave Syer
 *
 */
@SlimConfiguration(module = { HttpMessageConvertersAutoConfigurationModule.class,
		ReactorCoreAutoConfigurationModule.class })
public class ReactiveWebServerFactoryAutoConfigurationModule implements Module {

	@Override
	public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
		return Arrays.asList(new Initializer(),
				WebFluxAutoConfigurationGenerated.initializer(),
				HttpHandlerAutoConfigurationGenerated.initializer(),
				ErrorWebFluxAutoConfigurationGenerated.initializer());
	}

	@InitializerMapping(ReactiveWebServerFactoryAutoConfiguration.class)
	static class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {
		@Override
		public void initialize(GenericApplicationContext context) {
			ConditionService conditions = context.getBeanFactory()
					.getBean(ConditionService.class);
			if (conditions.matches(ReactiveWebServerFactoryAutoConfiguration.class)) {
				// This one has to jump the queue. TODO: try something different
				ReactiveWebServerFactoryAutoConfigurationGenerated.initializer()
						.initialize(context);
				context.registerBean(ServerProperties.class,
						() -> new ServerProperties());
				context.registerBean(WebFluxProperties.class,
						() -> new WebFluxProperties());
				context.registerBean(ResourceProperties.class,
						() -> new ResourceProperties());
			}
		}
	}

}
