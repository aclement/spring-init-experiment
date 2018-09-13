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

package boot.autoconfigure.reactor;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.reactor.core.ReactorCoreAutoConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import static slim.SlimRegistry.register;

import slim.ConditionService;
import slim.Module;

/**
 * @author Dave Syer
 *
 */
public class ReactorCoreAutoConfigurationModule implements Module {

	private static class AutoConfigurationPostProcessor
			implements BeanDefinitionRegistryPostProcessor {

		private final ConditionService conditions;

		private AutoConfigurationPostProcessor(ConditionService conditions) {
			this.conditions = conditions;
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory context)
				throws BeansException {
		}

		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
				throws BeansException {
			if (conditions.matches(ReactorCoreAutoConfiguration.class)) {
				register(registry, ReactorCoreAutoConfiguration.class,
						() -> new ReactorCoreAutoConfiguration());
			}
		}
	}

	@Override
	public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
		return Arrays.asList(ReactorCoreAutoConfigurationModule.initializer());
	}

	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return context -> context.registerBean(AutoConfigurationPostProcessor.class,
				() -> new AutoConfigurationPostProcessor(
						context.getBean(ConditionService.class)));
	}

}
