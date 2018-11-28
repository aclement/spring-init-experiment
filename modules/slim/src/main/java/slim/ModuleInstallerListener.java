/*
 * Copyright 2016-2017 the original author or authors.
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebApplicationContext;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Dave Syer
 *
 */
public class ModuleInstallerListener implements SmartApplicationListener {

	private static final Log logger = LogFactory.getLog(ModuleInstallerListener.class);

	// TODO: make this class stateless
	private Collection<ApplicationContextInitializer<GenericApplicationContext>> initializers = new LinkedHashSet<>();

	private Collection<ApplicationContextInitializer<GenericApplicationContext>> autos = new LinkedHashSet<>();

	private Set<Class<? extends ApplicationContextInitializer<?>>> added = new LinkedHashSet<>();

	private Set<String> autoTypeNames = new LinkedHashSet<>();

	private Map<Class<?>, Class<? extends ApplicationContextInitializer<?>>> autoTypes = new HashMap<>();

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return ApplicationContextInitializedEvent.class.isAssignableFrom(eventType)
				|| ApplicationEnvironmentPreparedEvent.class.isAssignableFrom(eventType);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationContextInitializedEvent) {
			ApplicationContextInitializedEvent initialized = (ApplicationContextInitializedEvent) event;
			ConfigurableApplicationContext context = initialized.getApplicationContext();
			if (!(context instanceof GenericApplicationContext)) {
				throw new IllegalStateException(
						"ApplicationContext must be a GenericApplicationContext");
			}
			GenericApplicationContext generic = (GenericApplicationContext) context;
			ConditionService conditions = new ModuleInstallerConditionService(generic,
					context.getEnvironment(), context);
			initialize(generic, conditions);
			if (!isEnabled(context.getEnvironment())) {
				return;
			}
			functional(generic, conditions);
			apply(generic, initialized.getSpringApplication(), conditions);
		}
		else if (event instanceof ApplicationEnvironmentPreparedEvent) {
			ApplicationEnvironmentPreparedEvent prepared = (ApplicationEnvironmentPreparedEvent) event;
			if (!isEnabled(prepared.getEnvironment())) {
				return;
			}
			SpringApplication application = prepared.getSpringApplication();
			WebApplicationType type = application.getWebApplicationType();
			Class<?> contextType = getApplicationContextType(application);
			if (type == WebApplicationType.NONE) {
				if (contextType == AnnotationConfigApplicationContext.class
						|| contextType == null) {
					application
							.setApplicationContextClass(GenericApplicationContext.class);
				}
			}
			else if (type == WebApplicationType.REACTIVE) {
				if (contextType == AnnotationConfigReactiveWebApplicationContext.class) {
					application.setApplicationContextClass(
							ReactiveWebServerApplicationContext.class);
				}
			}
			else if (type == WebApplicationType.SERVLET) {
				if (contextType == AnnotationConfigServletWebServerApplicationContext.class) {
					application.setApplicationContextClass(
							ServletWebServerApplicationContext.class);
				}
			}
		}
	}

	private Class<?> getApplicationContextType(SpringApplication application) {
		Field field = ReflectionUtils.findField(SpringApplication.class,
				"applicationContextClass");
		ReflectionUtils.makeAccessible(field);
		try {
			return (Class<?>) ReflectionUtils.getField(field, application);
		}
		catch (Exception e) {
			return null;
		}
	}

	private boolean isEnabled(ConfigurableEnvironment environment) {
		return environment.getProperty("spring.functional.enabled", Boolean.class, true);
	}

	private void functional(GenericApplicationContext context,
			ConditionService conditions) {
		context.registerBean(
				AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME,
				SlimConfigurationClassPostProcessor.class,
				() -> new SlimConfigurationClassPostProcessor(autoTypes));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
	}

	private void initialize(GenericApplicationContext context,
			ConditionService conditions) {
		context.registerBean(ConditionService.class, () -> conditions);
		context.registerBean(ImportRegistrars.class,
				() -> new ModuleInstallerImportRegistrars(context));
		this.autoTypeNames = new HashSet<>(SpringFactoriesLoader.loadFactoryNames(
				EnableAutoConfiguration.class, context.getClassLoader()));
		for (String autoName : autoTypeNames) {
			String typeName = autoName + "Initializer";
			if (ClassUtils.isPresent(typeName, context.getClassLoader())) {
				@SuppressWarnings("unchecked")
				Class<? extends ApplicationContextInitializer<?>> module = (Class<? extends ApplicationContextInitializer<?>>) ClassUtils
						.resolveClassName(typeName, context.getClassLoader());
				try {
					this.autoTypes.put(ClassUtils.resolveClassName(autoName,
							context.getClassLoader()), module);
				}
				catch (Throwable t) {
					throw new IllegalStateException(
							"Problem processing @Import/configurations() on "
									+ module.getName(),
							t);
				}
			}
		}
	}

	private void apply(GenericApplicationContext context) {
		List<ApplicationContextInitializer<GenericApplicationContext>> initializers = new ArrayList<>();
		for (ApplicationContextInitializer<GenericApplicationContext> result : this.initializers) {
			initializers.add(result);
		}
		OrderComparator.sort(initializers);
		if (logger.isDebugEnabled()) {
			logger.debug("Applying initializers: " + initializers);
		}
		for (ApplicationContextInitializer<GenericApplicationContext> initializer : initializers) {
			initializer.initialize(context);
		}
		initializers = new ArrayList<>();
		for (ApplicationContextInitializer<GenericApplicationContext> result : this.autos) {
			initializers.add(result);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Applying autoconfig " + initializers);
		}
		// TODO: sort into autoconfiguration order as well
		OrderComparator.sort(initializers);
		for (ApplicationContextInitializer<GenericApplicationContext> initializer : initializers) {
			initializer.initialize(context);
		}
	}

	private void apply(GenericApplicationContext context, SpringApplication application,
			ConditionService conditions) {
		ImportRegistrars registrars = context.getBeanFactory()
				.getBean(ImportRegistrars.class);
		Set<Class<?>> seen = new HashSet<>();
		for (Object source : application.getAllSources()) {
			Class<?> type = null;
			if (source instanceof Class) {
				type = (Class<?>) source;
			}
			else if (source instanceof String && ClassUtils.isPresent((String) source,
					application.getClassLoader())) {
				type = ClassUtils.resolveClassName((String) source,
						application.getClassLoader());
			}
			if (type != null) {
				extract(context, conditions, registrars, type, seen);
			}
		}
		apply(context);
	}

	private void extract(GenericApplicationContext context, ConditionService conditions,
			ImportRegistrars registrars, Class<?> beanClass, Set<Class<?>> seen) {
		if (conditions.matches(beanClass)) {
			// Causes inclusion of SampleApplicationModule if beanClass is
			// SampleApplication (without this we'll only include SampleApplicationModule
			// if it depends on other autoconfig that pulls in SampleApplicationModule!)
			maybeAddInitializer(context, registrars, beanClass, beanClass);
			processImports(context, conditions, registrars, beanClass, seen);
		}
	}

	private void maybeAddInitializer(GenericApplicationContext context,
			ImportRegistrars registrars, Class<?> source, Class<?> beanClass) {
		if (autoTypes.containsKey(beanClass)) {
			addInitializer(autoTypes.get(beanClass));
		}
		else if (ApplicationContextInitializer.class.isAssignableFrom(beanClass)) {
			@SuppressWarnings("unchecked")
			Class<? extends ApplicationContextInitializer<?>> initializer = (Class<? extends ApplicationContextInitializer<?>>) beanClass;
			addInitializer(initializer);
		}
		else if (ClassUtils.isPresent(beanClass.getName() + "Initializer",
				context.getClassLoader())) {
			@SuppressWarnings("unchecked")
			Class<? extends ApplicationContextInitializer<?>> initializer = (Class<? extends ApplicationContextInitializer<?>>) ClassUtils
					.resolveClassName(beanClass.getName() + "Initializer",
							context.getClassLoader());
			addInitializer(initializer);
		}
		else if (ImportBeanDefinitionRegistrar.class.isAssignableFrom(beanClass)
				|| ImportSelector.class.isAssignableFrom(beanClass)) {
			registrars.add(source, beanClass);
		}
	}

	private void processImports(GenericApplicationContext context,
			ConditionService conditions, ImportRegistrars registrars, Class<?> beanClass,
			Set<Class<?>> seen) {
		if (!seen.contains(beanClass)) {
			XmlBeanDefinitionReader xml = null;
			if (conditions.matches(beanClass)) {
				// logger.info("processing beanclass: "+beanClass);
				Set<Import> imports = AnnotatedElementUtils
						.findAllMergedAnnotations(beanClass, Import.class);
				if (imports != null) {
					for (Import imported : imports) {
						for (Class<?> value : imported.value()) {
							if (logger.isDebugEnabled()) {
								logger.debug("Import: " + value);
							}
							maybeAddInitializer(context, registrars, beanClass, value);
							processImports(context, conditions, registrars, value, seen);
							seen.add(value);
						}
					}
				}
				Set<ImportResource> resources = AnnotatedElementUtils
						.findAllMergedAnnotations(beanClass, ImportResource.class);
				if (resources != null) {
					for (ImportResource resource : resources) {
						for (String value : resource.value()) {
							if (logger.isDebugEnabled()) {
								logger.debug("ImportResource: " + value);
							}
							// Assume XML. No support for groovy as yet.
							if (xml == null) {
								xml = new XmlBeanDefinitionReader(context);
							}
							xml.loadBeanDefinitions(context.getResource(value));
						}
					}
				}
			}
			seen.add(beanClass);
		}
	}

	@SuppressWarnings("unchecked")
	private void addInitializer(Class<? extends ApplicationContextInitializer<?>> type) {
		if (type == null || this.added.contains(type)) {
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Adding initializer: " + type);
		}
		this.added.add(type);
		if (this.autoTypeNames.contains(type.getName())) {
			this.autos.add(BeanUtils.instantiateClass(type,
					ApplicationContextInitializer.class));
		}
		else {
			initializers.add(BeanUtils.instantiateClass(type,
					ApplicationContextInitializer.class));
		}
	}

	public static void invokeAwareMethods(Object target, Environment environment,
			ResourceLoader resourceLoader, BeanDefinitionRegistry registry) {

		if (target instanceof Aware) {
			if (target instanceof BeanClassLoaderAware) {
				ClassLoader classLoader = (registry instanceof ConfigurableBeanFactory
						? ((ConfigurableBeanFactory) registry).getBeanClassLoader()
						: resourceLoader.getClassLoader());
				if (classLoader != null) {
					((BeanClassLoaderAware) target).setBeanClassLoader(classLoader);
				}
			}
			if (target instanceof BeanFactoryAware && registry instanceof BeanFactory) {
				((BeanFactoryAware) target).setBeanFactory((BeanFactory) registry);
			}
			if (target instanceof EnvironmentAware) {
				((EnvironmentAware) target).setEnvironment(environment);
			}
			if (target instanceof ResourceLoaderAware) {
				((ResourceLoaderAware) target).setResourceLoader(resourceLoader);
			}
		}
	}

}

class SlimConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor,
		BeanClassLoaderAware, PriorityOrdered {

	private ClassLoader classLoader;
	private Map<Class<?>, Class<? extends ApplicationContextInitializer<?>>> autoTypes;

	public SlimConfigurationClassPostProcessor(
			Map<Class<?>, Class<? extends ApplicationContextInitializer<?>>> autoTypes) {
		this.autoTypes = autoTypes;
	}

	@Override
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
			Class<?> beanClass = findBeanClass(beanDefinition);
			if (beanClass != null) {
				if (slimConfiguration(beanClass)) {
					// In an app with mixed @Configuration and initializers we would have
					// to do more than this...
					if (registry instanceof ConfigurableListableBeanFactory) {
						ConfigurableListableBeanFactory listable = (ConfigurableListableBeanFactory) registry;
						if (listable.getBeanNamesForType(beanClass, false,
								false).length > 1) {
							// Some ApplicationContext classes register @Configuration
							// classes as bean definitions so we need to remove that one
							registry.removeBeanDefinition(beanName);
						}
					}
				}
			}
		}
	}

	private Class<?> findBeanClass(BeanDefinition beanDefinition) {
		String className = beanDefinition.getBeanClassName();
		if (className == null || beanDefinition.getFactoryMethodName() != null) {
			return null;
		}
		try {
			return ClassUtils.resolveClassName(className, classLoader);
		}
		catch (Throwable e) {
			return null;
		}
	}

	private boolean slimConfiguration(Class<?> beanClass) {
		if (autoTypes.containsKey(beanClass)) {
			return true;
		}
		return beanClass.isAnnotationPresent(SpringBootConfiguration.class);
	}

}
