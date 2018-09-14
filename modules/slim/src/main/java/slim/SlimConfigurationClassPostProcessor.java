/*
 * Copyright 2002-2018 the original author or authors.
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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

public class SlimConfigurationClassPostProcessor implements
		BeanDefinitionRegistryPostProcessor, BeanClassLoaderAware, PriorityOrdered {

	private ClassLoader classLoader;

	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 1;
	}

	public void setMetadataReaderFactory(MetadataReaderFactory metadataReaderFactory) {
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
			throws BeansException {
		String[] candidateNames = registry.getBeanDefinitionNames();
		for (String beanName : candidateNames) {
			BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
			String className = beanDefinition.getBeanClassName();
			if (className == null || beanDefinition.getFactoryMethodName() != null) {
				continue;
			}
			Class<?> beanClass = ClassUtils.resolveClassName(className, classLoader);
			if (slimConfiguration(beanClass)) {
				// In an app with mixed @Configuration and initializers we would have to
				// do more than this...
				if (registry instanceof ConfigurableListableBeanFactory) {
					ConfigurableListableBeanFactory listable = (ConfigurableListableBeanFactory) registry;
					if (listable.getBeanNamesForType(beanClass, false,
							false).length > 1) {
						// Some ApplicationContext classes register @Configuration classes
						// as bean definitions so we need to remove that one
						registry.removeBeanDefinition(beanName);
					}
				}
				// TODO: mark the bean definition somehow so it doesn't get
				// processed by ConfigurationClassPostProcessor
			}
		}
	}

	private boolean slimConfiguration(Class<?> beanClass) {
		SlimConfiguration slim = beanClass.getAnnotation(SlimConfiguration.class);
		if (slim != null) {
			return true;
		}
		return false;
	}

}
