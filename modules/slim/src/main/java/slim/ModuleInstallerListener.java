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

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
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
import org.springframework.boot.WebApplicationType;
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
import org.springframework.context.annotation.Configuration;
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
import org.springframework.core.type.StandardAnnotationMetadata;
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

	private Set<Class<? extends Module>> types = new LinkedHashSet<>();

	private Set<String> autoTypeNames = new LinkedHashSet<>();

	private Map<Class<?>, Class<? extends Module>> autoTypes = new HashMap<>();

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
				() -> new SlimConfigurationClassPostProcessor());
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
	}

	private void initialize(GenericApplicationContext context,
			ConditionService conditions) {
		context.registerBean(ConditionService.class, () -> conditions);
		this.autoTypeNames = new HashSet<>(SpringFactoriesLoader
				.loadFactoryNames(Module.class, context.getClassLoader()));
		for (String typeName : autoTypeNames) {
			if (ClassUtils.isPresent(typeName, context.getClassLoader())) {
				@SuppressWarnings("unchecked")
				Class<? extends Module> module = (Class<? extends Module>) ClassUtils
						.resolveClassName(typeName, context.getClassLoader());
				try {
					// Use asm rather than this - this makes it difficult to deal with
					// problems resolving the types referred to in the annotation
					// Import mapping = module.getAnnotation(Import.class);
					ImportAnnotationClassReader iacr = new ImportAnnotationClassReader();
					new ClassReader(context.getClassLoader().getResourceAsStream(
							module.getName().replace(".", "/") + ".class")).accept(iacr,
									ClassReader.SKIP_CODE);
					iacr.getImportReferences();

					if (iacr.importAnnotationExists) {
						for (String type : iacr.getImportReferences()) {
							try {
								Class<?> loadedType = ClassUtils.forName(type,
										context.getClassLoader());
								this.autoTypes.put(loadedType, module);
							}
							catch (Throwable cnfe) {
								// skip it... effectively there is no support for that but
								// it doesnt matter because it isnt around?
							}
						}
					}
					else {
						List<Class<?>> configurations = BeanUtils
								.instantiateClass(module, Module.class).configurations();
						for (Class<?> type : configurations) {
							this.autoTypes.put(type, module);
						}
					}
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
		logger.info("Applying initializers");
		List<ApplicationContextInitializer<GenericApplicationContext>> initializers = new ArrayList<>();
		for (ApplicationContextInitializer<GenericApplicationContext> result : this.initializers) {
			initializers.add(result);
		}
		OrderComparator.sort(initializers);
		for (ApplicationContextInitializer<GenericApplicationContext> initializer : initializers) {
			initializer.initialize(context);
		}
		logger.info("Applying autoconfig");
		initializers = new ArrayList<>();
		for (ApplicationContextInitializer<GenericApplicationContext> result : this.autos) {
			initializers.add(result);
		}
		// TODO: sort into autoconfiguration order as well
		OrderComparator.sort(initializers);
		for (ApplicationContextInitializer<GenericApplicationContext> initializer : initializers) {
			initializer.initialize(context);
		}
	}

	private void apply(GenericApplicationContext context, SpringApplication application,
			ConditionService conditions) {
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
				extract(context, conditions, type, seen);
			}
		}
		apply(context);
	}

	private void extract(GenericApplicationContext context, ConditionService conditions,
			Class<?> beanClass, Set<Class<?>> seen) {
		if (conditions.matches(beanClass)) {
			processImports(context, conditions, beanClass, seen);
		}
	}

	private void processImports(GenericApplicationContext context,
			ConditionService conditions, Class<?> beanClass, Set<Class<?>> seen) {
		if (!seen.contains(beanClass)) {
			XmlBeanDefinitionReader xml = null;
			if (conditions.matches(beanClass)) {
				Set<Import> imports = AnnotatedElementUtils
						.findAllMergedAnnotations(beanClass, Import.class);
				if (imports != null) {
					for (Import imported : imports) {
						for (Class<?> value : imported.value()) {
							logger.debug("Import: " + value);
							Class<? extends Module> type = this.autoTypes.get(value);
							if (type != null) {
								addModule(type);
							}
							else if (Module.class.isAssignableFrom(value)) {
								@SuppressWarnings("unchecked")
								Class<? extends Module> module = (Class<? extends Module>) value;
								addModule(module);
							}
							else if (ImportBeanDefinitionRegistrar.class
									.isAssignableFrom(value)) {
								ImportBeanDefinitionRegistrar registrar = BeanUtils
										.instantiateClass(value,
												ImportBeanDefinitionRegistrar.class);
								invokeAwareMethods(registrar, context.getEnvironment(),
										context, context);
								registrar.registerBeanDefinitions(
										new StandardAnnotationMetadata(value), context);
							}
							else if (ImportSelector.class.isAssignableFrom(value)) {
								ImportSelector registrar = BeanUtils
										.instantiateClass(value, ImportSelector.class);
								invokeAwareMethods(registrar, context.getEnvironment(),
										context, context);
								String[] selected = registrar.selectImports(
										new StandardAnnotationMetadata(value));
								for (String select : selected) {
									if (ClassUtils.isPresent(select,
											context.getClassLoader())) {
										Class<?> clazz = ClassUtils.resolveClassName(
												select, context.getClassLoader());
										if (clazz.getAnnotation(
												Configuration.class) != null) {
											processImports(context, conditions, clazz,
													seen);
										}
										else {
											context.registerBean(clazz);
										}
									}
								}
								// TODO: support for deferred import selector
							}
							processImports(context, conditions, value, seen);
							seen.add(value);
						}
					}
				}
				Set<ImportResource> resources = AnnotatedElementUtils
						.findAllMergedAnnotations(beanClass, ImportResource.class);
				if (resources != null) {
					for (ImportResource resource : resources) {
						for (String value : resource.value()) {
							logger.debug("ImportResource: " + value);
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

	private void addModule(Class<? extends Module> type) {
		if (type == null || this.types.contains(type)) {
			return;
		}
		logger.debug("Module: " + type);
		this.types.add(type);
		if (this.autoTypeNames.contains(type.getName())) {
			this.autos.addAll(
					BeanUtils.instantiateClass(type, Module.class).initializers());
		}
		else {
			initializers.addAll(
					BeanUtils.instantiateClass(type, Module.class).initializers());
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

	static class ImportAnnotationClassReader extends ClassVisitor {

		private boolean importAnnotationExists = false;
		private List<String> importReferences = new ArrayList<String>();

		public ImportAnnotationClassReader() {
			super(Opcodes.ASM7);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
			if (descriptor.equals("Lorg/springframework/context/annotation/Import;")) {
				importAnnotationExists = true;
				return new ImportAnnotationVisitor();
			}
			else {
				return super.visitAnnotation(descriptor, visible);
			}
		}

		class ImportAnnotationVisitor extends AnnotationVisitor {

			public ImportAnnotationVisitor() {
				super(Opcodes.ASM7);
			}

			@Override
			public AnnotationVisitor visitArray(String name) {
				if (name.equals("value")) {
					// crude
					return this;
				}
				else {
					return super.visitArray(name);
				}
			}

			@Override
			public void visit(String name, Object value) {
				if (name == null) {
					importReferences.add(((Type) value).getClassName());
				}
				super.visit(name, value);
			}

		}

		public List<String> getImportReferences() {
			return importReferences;
		}

		public boolean importAnnotationExists() {
			return importAnnotationExists;
		}

	}

}

class SlimConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor,
		BeanClassLoaderAware, PriorityOrdered {

	private ClassLoader classLoader;

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
		Import slim = beanClass.getAnnotation(Import.class);
		if (slim != null) {
			for (Class<?> module : slim.value()) {
				if (Module.class.isAssignableFrom(module)) {
					return true;
				}
			}
		}
		return false;
	}

}
