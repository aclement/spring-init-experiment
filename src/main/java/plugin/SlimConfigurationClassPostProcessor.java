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

package plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

public class SlimConfigurationClassPostProcessor
        implements BeanDefinitionRegistryPostProcessor, PriorityOrdered,
        BeanClassLoaderAware, ApplicationContextAware {

    private static final Log logger = LogFactory
            .getLog(SlimConfigurationClassPostProcessor.class);

    private Collection<Class<ApplicationContextInitializer<GenericApplicationContext>>> initializers = new LinkedHashSet<>();

    private GenericApplicationContext context;

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
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (context instanceof GenericApplicationContext) {
            this.context = (GenericApplicationContext) context;
        }
        else {
            throw new UnsupportedOperationException(
                    "Cannot create slim configuration (not GenericApplicationContext): "
                            + context);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        if (context != null) {
            logger.info("Applying initializers");
            List<ApplicationContextInitializer<GenericApplicationContext>> initializers = new ArrayList<>();
            for (Class<ApplicationContextInitializer<GenericApplicationContext>> type : this.initializers) {
                ApplicationContextInitializer<GenericApplicationContext> result = (ApplicationContextInitializer<GenericApplicationContext>) BeanUtils
                        .instantiateClass(type);
                initializers.add(result);
            }
            OrderComparator.sort(initializers);
            for (ApplicationContextInitializer<GenericApplicationContext> initializer : initializers) {
                initializer.initialize(context);
            }
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
            throws BeansException {
        String[] candidateNames = registry.getBeanDefinitionNames();
        for (String beanName : candidateNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (slimConfiguration(beanDefinition)) {
                registry.removeBeanDefinition(beanName);
            }
        }
    }

    private boolean slimConfiguration(BeanDefinition beanDef) {
        String className = beanDef.getBeanClassName();
        if (className == null || beanDef.getFactoryMethodName() != null) {
            return false;
        }
        Class<?> beanClass = ClassUtils.resolveClassName(className, classLoader);
        return extract(beanClass);
    }

    public boolean extract(Class<?> beanClass) {
        SlimConfiguration slim = beanClass.getAnnotation(SlimConfiguration.class);
        if (slim != null) {
            Class<ApplicationContextInitializer<GenericApplicationContext>>[] types = slim
                    .type();
            for (Class<ApplicationContextInitializer<GenericApplicationContext>> type : types) {
                logger.info("Slim initializer: " + type);
                initializers.add(type);
                Import importer = beanClass.getAnnotation(Import.class);
                if (importer != null) {
                    for (Class<?> imported : importer.value()) {
                        extract(imported);
                    }
                }
            }
            return true;
        }
        return false;
    }

}
