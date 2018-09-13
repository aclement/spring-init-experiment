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

package boot.autoconfigure.gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.boot.autoconfigure.gson.GsonProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import static slim.SlimRegistry.register;

import boot.autoconfigure.context.ContextAutoConfigurationModule;
import slim.ConditionService;
import slim.Module;
import slim.SlimConfiguration;

/**
 * @author Dave Syer
 *
 */
@SlimConfiguration(module=ContextAutoConfigurationModule.class)
public class GsonAutoConfigurationModule implements Module {

	@Override
	public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
		return Arrays.asList(GsonAutoConfigurationModule.initializer());
	}

	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return new Initializer();
	}

	private static class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			// Use a BeanDefinitionRegistryPostProcessor so that user configuration can
			// take precedence (the same way that regular AutoConfiguration works).
			context.registerBean(AutoConfigurationPostProcessor.class,
					() -> new AutoConfigurationPostProcessor(
							context.getBean(ConditionService.class)));
		}

	}

	private static class AutoConfigurationPostProcessor
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
			if (conditions.matches(GsonAutoConfiguration.class)) {
				register(registry, GsonProperties.class, () -> new GsonProperties());
				register(registry, GsonAutoConfiguration.class);
				if (conditions.matches(GsonAutoConfiguration.class, GsonBuilder.class)) {
					register(registry, "gsonBuilder", GsonBuilder.class,
							() -> context.getBean(GsonAutoConfiguration.class)
									.gsonBuilder(new ArrayList<>(context
											.getBeansOfType(GsonBuilderCustomizer.class)
											.values())));
				}
				if (conditions.matches(GsonAutoConfiguration.class, Gson.class)) {
					register(registry, "gson", Gson.class,
							() -> context.getBean(GsonAutoConfiguration.class)
									.gson(context.getBean(GsonBuilder.class)));
				}
				register(registry, "standardGsonBuilderCustomizer",
						GsonBuilderCustomizer.class,
						() -> context.getBean(GsonAutoConfiguration.class)
								.standardGsonBuilderCustomizer(
										context.getBean(GsonProperties.class)));
			}
		}

	}

}
