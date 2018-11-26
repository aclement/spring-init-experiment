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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * @author Dave Syer
 *
 */
public class ModuleInstallerImportRegistrars
		implements BeanDefinitionRegistryPostProcessor, ImportRegistrars {

	private Map<Class<?>, Class<?>> registrars = new LinkedHashMap<>();

	private GenericApplicationContext context;

	public ModuleInstallerImportRegistrars(GenericApplicationContext context) {
		this.context = context;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {
	}

	@Override
	public void add(Class<?> importer, Class<?> registrar) {
		this.registrars.put(registrar, importer);
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
			throws BeansException {
		for (Class<?> type : new LinkedHashSet<>(registrars.keySet())) {
			if (ImportSelector.class.isAssignableFrom(type)) {
				ImportSelector registrar = (ImportSelector) context
						.getAutowireCapableBeanFactory().createBean(type);
				String[] selected = registrar.selectImports(
						new StandardAnnotationMetadata(registrars.get(type)));
				for (String select : selected) {
					if (ClassUtils.isPresent(select, context.getClassLoader())) {
						Class<?> clazz = ClassUtils.resolveClassName(select,
								context.getClassLoader());
						if (clazz.getAnnotation(Configuration.class) != null) {
							// recurse?
						}
						// TODO this branch still necessary?
						else if (ImportBeanDefinitionRegistrar.class
								.isAssignableFrom(clazz)) {
							add(type, clazz);
						}
						else {
							context.registerBean(clazz);
						}
					}
				}
			}
		}
		for (Class<?> type : registrars.keySet()) {
			if (ImportBeanDefinitionRegistrar.class.isAssignableFrom(type)) {
				Object bean = context.getAutowireCapableBeanFactory().createBean(type);
				ImportBeanDefinitionRegistrar registrar = (ImportBeanDefinitionRegistrar) bean;
				registrar.registerBeanDefinitions(
						new StandardAnnotationMetadata(registrars.get(type)), registry);
			}
		}
	}

}
