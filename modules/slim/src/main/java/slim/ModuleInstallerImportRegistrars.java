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

import java.util.LinkedHashSet;
import java.util.Set;

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

	private Set<Imported> registrars = new LinkedHashSet<>();

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
		this.registrars.add(new Imported(importer, registrar));
	}

	@Override
	public void add(Class<?> importer, String typeName) {
		this.registrars.add(new Imported(importer, typeName, context.getClassLoader()));
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
			throws BeansException {
		Set<Imported> added = new LinkedHashSet<>();
		for (Imported imported : registrars) {
			Class<?> type = imported.getType();
			if (type != null) {
				if (ImportSelector.class.isAssignableFrom(type)) {
					ImportSelector registrar = (ImportSelector) context
							.getAutowireCapableBeanFactory().createBean(type);
					String[] selected = registrar.selectImports(
							new StandardAnnotationMetadata(imported.getSource()));
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
								added.add(new Imported(imported.getSource(), clazz));
							}
							else {
								context.registerBean(clazz);
							}
						}
					}
				}
				else if (ImportBeanDefinitionRegistrar.class.isAssignableFrom(type)) {
					importRegistrar(registry, imported);
				}
				else {
					context.registerBean(type);
				}
			}
		}
		for (Imported imported : added) {
			if (!registrars.contains(imported)) {
				Class<?> type = imported.getType();
				if (type != null
						&& ImportBeanDefinitionRegistrar.class.isAssignableFrom(type)) {
					importRegistrar(registry, imported);
				}
			}
		}
	}

	public void importRegistrar(BeanDefinitionRegistry registry, Imported imported) {
		Class<?> type = imported.getType();
		Object bean = context.getAutowireCapableBeanFactory().createBean(type);
		ImportBeanDefinitionRegistrar registrar = (ImportBeanDefinitionRegistrar) bean;
		registrar.registerBeanDefinitions(
				new StandardAnnotationMetadata(imported.getSource()), registry);
	}

	private static class Imported {
		private Class<?> source;
		private String typeName;
		private Class<?> type;

		public Imported(Class<?> source, Class<?> type) {
			this.source = source;
			this.type = type;
			this.typeName = type.getName();
		}

		private Class<?> resolve(ClassLoader classLoader, String typeName) {
			if (ClassUtils.isPresent(typeName, classLoader)) {
				Class<?> clazz = ClassUtils.resolveClassName(typeName, classLoader);
				return clazz;
			}
			return null;
		}

		public Imported(Class<?> source, String typeName, ClassLoader classLoader) {
			this.source = source;
			this.type = resolve(classLoader, typeName);
			this.typeName = type==null ? typeName : type.getName();
		}

		public Class<?> getSource() {
			return this.source;
		}

		public Class<?> getType() {
			return this.type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((this.source == null) ? 0 : this.source.getName().hashCode());
			result = prime * result
					+ ((this.typeName == null) ? 0 : this.typeName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Imported other = (Imported) obj;
			if (this.source == null) {
				if (other.source != null)
					return false;
			}
			else if (!this.source.equals(other.source))
				return false;
			if (this.typeName == null) {
				if (other.typeName != null)
					return false;
			}
			else if (!this.typeName.equals(other.typeName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Imported [source=" + this.source.getName()

					+ ", type=" + this.typeName + "]";
		}
	}
}
