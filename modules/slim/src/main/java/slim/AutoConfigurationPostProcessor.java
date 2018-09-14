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

package slim;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Dave Syer
 *
 */
public class AutoConfigurationPostProcessor
		implements BeanDefinitionRegistryPostProcessor {

	private final GenericApplicationContext context;
	private final List<ApplicationContextInitializer<GenericApplicationContext>> initializers;

	public AutoConfigurationPostProcessor(GenericApplicationContext context,
			List<ApplicationContextInitializer<GenericApplicationContext>> initializers) {
		this.context = context;
		this.initializers = initializers;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
			throws BeansException {
		for (ApplicationContextInitializer<GenericApplicationContext> initializer : initializers) {
			initializer.initialize(context);
		}
	}

}
