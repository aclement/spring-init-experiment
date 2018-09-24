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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;

/**
 * @author Dave Syer
 *
 */
public class SlimConfigurationInstaller implements SmartApplicationListener {

	private static final Log logger = LogFactory.getLog(SlimConfigurationInstaller.class);

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
			if (context.getEnvironment().getProperty("spring.functional.enabled",
					Boolean.class, true)) {
				GenericApplicationContext generic = (GenericApplicationContext) context;
				initialize(generic);
				extract(generic, initialized.getSpringApplication());
			}
		}
		else if (event instanceof ApplicationEnvironmentPreparedEvent) {
			ApplicationEnvironmentPreparedEvent prepared = (ApplicationEnvironmentPreparedEvent) event;
			SpringApplication application = prepared.getSpringApplication();
			WebApplicationType type = application.getWebApplicationType();
			if (type == WebApplicationType.NONE) {
				// TODO: uncomment this
				// (https://github.com/spring-projects/spring-boot/issues/14589)
				// application.setApplicationContextClass(GenericApplicationContext.class);
			}
			else if (type == WebApplicationType.REACTIVE) {
				application.setApplicationContextClass(
						ReactiveWebServerApplicationContext.class);
			}
			else if (type == WebApplicationType.SERVLET) {
				application.setApplicationContextClass(
						ServletWebServerApplicationContext.class);
			}
		}
	}

	private void initialize(GenericApplicationContext context) {
		context.registerBean(ConditionService.class,
				() -> new SlimConditionService(context, context.getEnvironment(),
						context));
		context.registerBean(
				AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME,
				SlimConfigurationClassPostProcessor.class);
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		this.autoTypeNames = new HashSet<>(SpringFactoriesLoader
				.loadFactoryNames(Module.class, context.getClassLoader()));
		for (String typeName : autoTypeNames) {
			if (ClassUtils.isPresent(typeName, context.getClassLoader())) {
				@SuppressWarnings("unchecked")
				Class<? extends Module> module = (Class<? extends Module>) ClassUtils
						.resolveClassName(typeName, context.getClassLoader());
				ModuleMapping mapping = module.getAnnotation(ModuleMapping.class);
				if (mapping != null) {
					for (Class<?> type : mapping.value()) {
						this.autoTypes.put(type, module);
					}
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

	private void extract(GenericApplicationContext context,
			SpringApplication application) {
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
				extract(type, seen);
			}
		}
		apply(context);
	}

	private void extract(Class<?> beanClass, Set<Class<?>> seen) {
		SlimConfiguration slim = beanClass.getAnnotation(SlimConfiguration.class);
		if (slim != null) {
			Class<? extends Module>[] types = slim.module();
			for (Class<? extends Module> type : types) {
				logger.debug("ModuleImport: " + type);
				addModule(type);
			}
		}
		processImports(beanClass, seen);
	}

	private void processImports(Class<?> beanClass, Set<Class<?>> seen) {
		if (!seen.contains(beanClass)) {
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
						processImports(value, seen);
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

}
