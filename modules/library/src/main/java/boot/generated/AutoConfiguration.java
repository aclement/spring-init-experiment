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

package boot.generated;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.boot.autoconfigure.gson.GsonProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessorRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

import static slim.SlimRegistry.register;

import slim.SlimConfiguration;

/**
 * @author Dave Syer
 *
 */
@Configuration
@EnableConfigurationProperties
@SlimConfiguration(module = AutoConfigurationModule.class)
public class AutoConfiguration {

	@ConditionalOnClass(Gson.class)
	@EnableConfigurationProperties(GsonProperties.class)
	// Copied from Spring Boot
	static class GsonAutoConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public GsonBuilder gsonBuilder(List<GsonBuilderCustomizer> customizers) {
			GsonBuilder builder = new GsonBuilder();
			customizers.forEach((c) -> c.customize(builder));
			return builder;
		}

		@Bean
		@ConditionalOnMissingBean
		public Gson gson(GsonBuilder gsonBuilder) {
			return gsonBuilder.create();
		}

		@Bean
		public StandardGsonBuilderCustomizer standardGsonBuilderCustomizer(
				GsonProperties gsonProperties) {
			return new StandardGsonBuilderCustomizer(gsonProperties);
		}

		private static final class StandardGsonBuilderCustomizer
				implements GsonBuilderCustomizer, Ordered {

			private final GsonProperties properties;

			StandardGsonBuilderCustomizer(GsonProperties properties) {
				this.properties = properties;
			}

			@Override
			public int getOrder() {
				return 0;
			}

			@Override
			public void customize(GsonBuilder builder) {
				GsonProperties properties = this.properties;
				PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
				map.from(properties::getGenerateNonExecutableJson)
						.toCall(builder::generateNonExecutableJson);
				map.from(properties::getExcludeFieldsWithoutExposeAnnotation)
						.toCall(builder::excludeFieldsWithoutExposeAnnotation);
				map.from(properties::getSerializeNulls).toCall(builder::serializeNulls);
				map.from(properties::getEnableComplexMapKeySerialization)
						.toCall(builder::enableComplexMapKeySerialization);
				map.from(properties::getDisableInnerClassSerialization)
						.toCall(builder::disableInnerClassSerialization);
				map.from(properties::getLongSerializationPolicy)
						.to(builder::setLongSerializationPolicy);
				map.from(properties::getFieldNamingPolicy)
						.to(builder::setFieldNamingPolicy);
				map.from(properties::getPrettyPrinting)
						.toCall(builder::setPrettyPrinting);
				map.from(properties::getLenient).toCall(builder::setLenient);
				map.from(properties::getDisableHtmlEscaping)
						.toCall(builder::disableHtmlEscaping);
				map.from(properties::getDateFormat).to(builder::setDateFormat);
			}

		}

	}

	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return new Initializer();
	}

	private static class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			// Use a BeanDefinitionRegistryPostProcessor so that user configuration can
			// take precedence (the same way that regular AutoConfiguration works).
			context.registerBean(AutoConfigurationPostProcessor.class);
		}

	}

	private static class AutoConfigurationPostProcessor
			implements BeanDefinitionRegistryPostProcessor {

		private ConfigurableListableBeanFactory context;

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory context)
				throws BeansException {
			this.context = context;
		}

		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
				throws BeansException {
			// TODO: how to get from @EnableConfigurationProperties to this?
			new ConfigurationPropertiesBindingPostProcessorRegistrar()
					.registerBeanDefinitions(null, registry);
			// @ConditionalOnClass(Gson.class)
			if (ClassUtils.isPresent("com.google.gson.Gson", null)) {
				register(registry, GsonProperties.class, () -> new GsonProperties());
				register(registry, GsonAutoConfiguration.class);
				// @ConditionalOnMissingBean
				if (context.getBeanNamesForType(GsonBuilder.class).length == 0) {
					register(registry, "gsonBuilder", GsonBuilder.class,
							() -> context.getBean(GsonAutoConfiguration.class)
									.gsonBuilder(new ArrayList<>(context
											.getBeansOfType(GsonBuilderCustomizer.class)
											.values())));
				}
				// @ConditionalOnMissingBean
				if (context.getBeanNamesForType(Gson.class).length == 0) {
					register(registry, "gson", Gson.class,
							() -> context.getBean(GsonAutoConfiguration.class)
									.gson(context.getBean(GsonBuilder.class)));
				}
				register(registry, "standardGsonBuilderCustomizer",
						GsonBuilderCustomizer.class,
						() -> context.getBean(GsonAutoConfiguration.class)
								.standardGsonBuilderCustomizer(
										context.getBean(GsonProperties.class)));
			}
		}

	}

}
