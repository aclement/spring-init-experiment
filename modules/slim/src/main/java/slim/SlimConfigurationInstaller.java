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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;

/**
 * @author Dave Syer
 *
 */
public class SlimConfigurationInstaller implements SpringApplicationRunListener {

	private static final Log logger = LogFactory.getLog(SlimConfigurationInstaller.class);

	private Collection<ApplicationContextInitializer<GenericApplicationContext>> initializers = new LinkedHashSet<>();

	private Collection<ApplicationContextInitializer<GenericApplicationContext>> autos = new LinkedHashSet<>();

	private Set<Class<? extends Module>> types = new LinkedHashSet<>();

	private Set<String> autoTypes = new LinkedHashSet<>();

	private final SpringApplication application;

	public SlimConfigurationInstaller(SpringApplication application, String[] args) {
		this.application = application;
	}

	private void initialize(GenericApplicationContext context) {
		context.registerBean(ConditionService.class,
				() -> new SlimConditionService(context, context.getEnvironment(),
						context));
		context.registerBean(
				AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME,
				SlimConfigurationClassPostProcessor.class);
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		this.autoTypes = new HashSet<>(SpringFactoriesLoader
				.loadFactoryNames(Module.class, context.getClassLoader()));
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

	private void extract(GenericApplicationContext context) {
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
				extract(type);
			}
		}
		apply(context);
	}

	private void extract(Class<?> beanClass) {
		SlimConfiguration slim = beanClass.getAnnotation(SlimConfiguration.class);
		if (slim != null) {
			Class<? extends Module>[] types = slim.module();
			for (Class<? extends Module> type : types) {
				if (this.types.contains(type)) {
					continue;
				}
				logger.info("Slim initializer: " + type);
				extract(type);
				this.types.add(type);
				if (this.autoTypes.contains(type.getName())) {
					this.autos.addAll(BeanUtils.instantiateClass(type, Module.class)
							.initializers());
				}
				else {
					initializers.addAll(BeanUtils.instantiateClass(type, Module.class)
							.initializers());
				}
			}
		}
	}

	@Override
	public void starting() {
	}

	@Override
	public void environmentPrepared(ConfigurableEnvironment environment) {
	}

	@Override
	public void contextPrepared(ConfigurableApplicationContext context) {
		GenericApplicationContext generic = (GenericApplicationContext) context;
		initialize(generic);
		extract(generic);
	}

	@Override
	public void contextLoaded(ConfigurableApplicationContext context) {
	}

	@Override
	public void started(ConfigurableApplicationContext context) {
	}

	@Override
	public void running(ConfigurableApplicationContext context) {
	}

	@Override
	public void failed(ConfigurableApplicationContext context, Throwable exception) {
	}

}
