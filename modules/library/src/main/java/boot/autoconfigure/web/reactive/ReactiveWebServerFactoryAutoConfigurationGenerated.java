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

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import slim.ConditionService;
import slim.InitializerMapping;

/**
 * @author Dave Syer
 *
 */
class ReactiveWebServerFactoryAutoConfigurationGenerated {

	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return new Initializer();
	}

	@InitializerMapping(ReactiveWebServerFactoryAutoConfiguration.class)
	private static final class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			ConditionService conditions = context.getBeanFactory()
					.getBean(ConditionService.class);
			if (conditions.matches(ReactiveWebServerFactoryAutoConfiguration.class)) {
				ReactiveWebServerFactoryAutoConfiguration config = new ReactiveWebServerFactoryAutoConfiguration();
				// From @Import
				context.registerBean(WebServerFactoryCustomizerBeanPostProcessor.class,
						() -> new WebServerFactoryCustomizerBeanPostProcessor());
				// From @Bean
				context.registerBean(ReactiveWebServerFactoryCustomizer.class,
						() -> config.reactiveWebServerFactoryCustomizer(
								context.getBean(ServerProperties.class)));
				// From @Import
				// TODO: add condition match from that (not visible here)
				// ReactiveWebServerFactoryConfiguration
				context.registerBean(NettyReactiveWebServerFactory.class,
						() -> new NettyReactiveWebServerFactory());
			}
		}
	}

}
