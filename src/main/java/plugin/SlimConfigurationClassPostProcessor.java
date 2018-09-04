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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.MultiValueMap;

public class SlimConfigurationClassPostProcessor implements
        BeanDefinitionRegistryPostProcessor, PriorityOrdered, ApplicationContextAware {

    private static final Log logger = LogFactory
            .getLog(SlimConfigurationClassPostProcessor.class);

    private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

    private List<ApplicationContextInitializer<GenericApplicationContext>> initializers = new ArrayList<>();

    private GenericApplicationContext context;

    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
    
    public void setMetadataReaderFactory(MetadataReaderFactory metadataReaderFactory) {
        this.metadataReaderFactory = metadataReaderFactory;
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
            ApplicationContextInitializer<GenericApplicationContext> initializer = slimConfiguration(
                    beanDefinition);
            if (initializer != null) {
                registry.removeBeanDefinition(beanName);
                initializers.add(initializer);
                logger.info("Slim initializer: " + beanName);
            }
        }
    }

    private ApplicationContextInitializer<GenericApplicationContext> slimConfiguration(
            BeanDefinition beanDef) {
        String className = beanDef.getBeanClassName();
        if (className == null || beanDef.getFactoryMethodName() != null) {
            return null;
        }
        AnnotationMetadata metadata;
        if (beanDef instanceof AnnotatedBeanDefinition && className.equals(
                ((AnnotatedBeanDefinition) beanDef).getMetadata().getClassName())) {
            // Can reuse the pre-parsed metadata from the given BeanDefinition...
            metadata = ((AnnotatedBeanDefinition) beanDef).getMetadata();
        }
        else if (beanDef instanceof AbstractBeanDefinition
                && ((AbstractBeanDefinition) beanDef).hasBeanClass()) {
            // Check already loaded Class if present...
            // since we possibly can't even load the class file for this Class.
            Class<?> beanClass = ((AbstractBeanDefinition) beanDef).getBeanClass();
            metadata = new StandardAnnotationMetadata(beanClass, true);
        }
        else {
            try {
                MetadataReader metadataReader = metadataReaderFactory
                        .getMetadataReader(className);
                metadata = metadataReader.getAnnotationMetadata();
            }
            catch (IOException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Could not find class file for introspecting configuration annotations: "
                                    + className,
                            ex);
                }
                return null;
            }
        }
        if (metadata.isAnnotated(SlimConfiguration.class.getName())) {
            MultiValueMap<String, Object> attrs = metadata
                    .getAllAnnotationAttributes(SlimConfiguration.class.getName());
            Class<?> type = (Class<?>) attrs.getFirst("type");
            @SuppressWarnings("unchecked")
            ApplicationContextInitializer<GenericApplicationContext> result = (ApplicationContextInitializer<GenericApplicationContext>) BeanUtils
                    .instantiateClass(type);
            return result;
        }
        return null;
    }

}
