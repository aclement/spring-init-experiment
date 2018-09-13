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

package boot.autoconfigure.web;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import static slim.SlimRegistry.register;

import boot.autoconfigure.context.ContextAutoConfigurationModule;
import boot.autoconfigure.reactor.ReactorCoreAutoConfigurationModule;
import slim.ConditionService;
import slim.Module;
import slim.SlimConfiguration;

/**
 * @author Dave Syer
 *
 */
@SlimConfiguration(module = { ContextAutoConfigurationModule.class,
		ReactorCoreAutoConfigurationModule.class })
public class ReactiveWebServerFactoryAutoConfigurationModule implements Module {

	@Override
	public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
		return Arrays.asList(ReactorCoreAutoConfigurationModule.initializer());
	}

	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return context -> context.registerBean(BeanDefinitionRegistryPostProcessor.class,
				() -> new AutoConfigurationPostProcessor(
						context.getBean(ConditionService.class)));
	}

	private static final class AutoConfigurationPostProcessor
			implements BeanDefinitionRegistryPostProcessor {

		private ConfigurableListableBeanFactory context;
		private final ConditionService conditions;

		private AutoConfigurationPostProcessor(ConditionService conditions) {
			this.conditions = conditions;
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory context)
				throws BeansException {
			this.context = context;
		}

		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
				throws BeansException {
			if (conditions.matches(ReactiveWebServerFactoryAutoConfiguration.class)) {
				ReactiveWebServerFactoryAutoConfiguration config = new ReactiveWebServerFactoryAutoConfiguration();
				// From @Import
				register(registry, WebServerFactoryCustomizerBeanPostProcessor.class,
						() -> new WebServerFactoryCustomizerBeanPostProcessor());
				// From @Bean
				register(registry, ReactiveWebServerFactoryCustomizer.class,
						() -> config.reactiveWebServerFactoryCustomizer(
								context.getBean(ServerProperties.class)));
				// From @Import
				// TODO: add condition match from that (not visible here)
				// ReactiveWebServerFactoryConfiguration
				register(registry, NettyReactiveWebServerFactory.class,
						() -> new NettyReactiveWebServerFactory());
			}
		}
	}

}
