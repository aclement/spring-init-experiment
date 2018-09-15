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

package boot.autoconfigure.jackson;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import boot.autoconfigure.context.ContextAutoConfigurationModule;
import slim.AutoConfigurationPostProcessor;
import slim.ConditionService;
import slim.Module;
import slim.SlimConfiguration;

/**
 * @author Dave Syer
 *
 */
@SlimConfiguration(module = ContextAutoConfigurationModule.class)
public class JacksonAutoConfigurationModule implements Module {

	@Override
	public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
		return Arrays.asList(JacksonAutoConfigurationModule.initializer());
	}

	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return context -> {
			context.registerBean(JacksonAutoConfigurationModule.class.getName(),
					BeanDefinitionRegistryPostProcessor.class,
					() -> new AutoConfigurationPostProcessor(context,
							Arrays.asList(new Initializer())));
		};
	}

	private static class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			ConditionService conditions = context.getBeanFactory()
					.getBean(ConditionService.class);
			if (conditions.matches(JacksonAutoConfiguration.class)) {
				context.registerBean(JacksonProperties.class,
						() -> new JacksonProperties());
				context.registerBean(JacksonAutoConfiguration.class);
				context.registerBean(JsonComponentModule.class, () -> context
						.getBean(JacksonAutoConfiguration.class).jsonComponentModule());
				// TODO: copy more code from package private nested classes
				if (conditions.matches(JacksonObjectMapperBuilderConfiguration.class)) {
					context.registerBean(JacksonObjectMapperBuilderConfiguration.class);
					if (conditions.matches(JacksonObjectMapperBuilderConfiguration.class,
							Jackson2ObjectMapperBuilder.class)) {
						context.registerBean(Jackson2ObjectMapperBuilder.class,
								() -> context.getBean(
										JacksonObjectMapperBuilderConfiguration.class)
										.jacksonObjectMapperBuilder(
												new ArrayList<>(context.getBeansOfType(
														Jackson2ObjectMapperBuilderCustomizer.class)
														.values())));
					}
				}
				if (conditions.matches(JacksonObjectMapperConfiguration.class)) {
					context.registerBean(JacksonObjectMapperConfiguration.class);
					context.registerBean("jacksonObjectMapper", ObjectMapper.class,
							() -> context.getBean(JacksonObjectMapperConfiguration.class)
									.jacksonObjectMapper(context
											.getBean(Jackson2ObjectMapperBuilder.class)));
				}
				if (conditions.matches(
						Jackson2ObjectMapperBuilderCustomizerConfiguration.class)) {
					context.registerBean(
							Jackson2ObjectMapperBuilderCustomizerConfiguration.class);
					context.registerBean("standardJackson2ObjectMapperBuilderCustomizer",
							Jackson2ObjectMapperBuilderCustomizer.class,
							() -> context.getBean(
									Jackson2ObjectMapperBuilderCustomizerConfiguration.class)
									.standardJacksonObjectMapperBuilderCustomizer(context,
											context.getBean(JacksonProperties.class)));
				}
			}
		}
	}

	// Copied from non-visible class in Boot
	@Configuration
	@ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
	static class JacksonObjectMapperConfiguration {

		@Bean
		@Primary
		@ConditionalOnMissingBean
		public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
			return builder.createXmlMapper(false).build();
		}

	}

	@Configuration
	@ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
	static class JacksonObjectMapperBuilderConfiguration {

		private final ApplicationContext applicationContext;

		JacksonObjectMapperBuilderConfiguration(ApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		@Bean
		@ConditionalOnMissingBean
		public Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder(
				List<Jackson2ObjectMapperBuilderCustomizer> customizers) {
			Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
			builder.applicationContext(this.applicationContext);
			customize(builder, customizers);
			return builder;
		}

		private void customize(Jackson2ObjectMapperBuilder builder,
				List<Jackson2ObjectMapperBuilderCustomizer> customizers) {
			for (Jackson2ObjectMapperBuilderCustomizer customizer : customizers) {
				customizer.customize(builder);
			}
		}

	}

	@Configuration
	@ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
	@EnableConfigurationProperties(JacksonProperties.class)
	static class Jackson2ObjectMapperBuilderCustomizerConfiguration {

		@Bean
		public StandardJackson2ObjectMapperBuilderCustomizer standardJacksonObjectMapperBuilderCustomizer(
				ApplicationContext applicationContext,
				JacksonProperties jacksonProperties) {
			return new StandardJackson2ObjectMapperBuilderCustomizer(applicationContext,
					jacksonProperties);
		}

		static final class StandardJackson2ObjectMapperBuilderCustomizer
				implements Jackson2ObjectMapperBuilderCustomizer, Ordered {

			private static final Map<?, Boolean> FEATURE_DEFAULTS;

			static {
				Map<Object, Boolean> featureDefaults = new HashMap<>();
				featureDefaults.put(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
						false);
				FEATURE_DEFAULTS = Collections.unmodifiableMap(featureDefaults);
			}

			private final ApplicationContext applicationContext;

			private final JacksonProperties jacksonProperties;

			StandardJackson2ObjectMapperBuilderCustomizer(
					ApplicationContext applicationContext,
					JacksonProperties jacksonProperties) {
				this.applicationContext = applicationContext;
				this.jacksonProperties = jacksonProperties;
			}

			@Override
			public int getOrder() {
				return 0;
			}

			@Override
			public void customize(Jackson2ObjectMapperBuilder builder) {

				if (this.jacksonProperties.getDefaultPropertyInclusion() != null) {
					builder.serializationInclusion(
							this.jacksonProperties.getDefaultPropertyInclusion());
				}
				if (this.jacksonProperties.getTimeZone() != null) {
					builder.timeZone(this.jacksonProperties.getTimeZone());
				}
				configureFeatures(builder, FEATURE_DEFAULTS);
				configureVisibility(builder, this.jacksonProperties.getVisibility());
				configureFeatures(builder, this.jacksonProperties.getDeserialization());
				configureFeatures(builder, this.jacksonProperties.getSerialization());
				configureFeatures(builder, this.jacksonProperties.getMapper());
				configureFeatures(builder, this.jacksonProperties.getParser());
				configureFeatures(builder, this.jacksonProperties.getGenerator());
				configureDateFormat(builder);
				configurePropertyNamingStrategy(builder);
				configureModules(builder);
				configureLocale(builder);
			}

			private void configureFeatures(Jackson2ObjectMapperBuilder builder,
					Map<?, Boolean> features) {
				features.forEach((feature, value) -> {
					if (value != null) {
						if (value) {
							builder.featuresToEnable(feature);
						}
						else {
							builder.featuresToDisable(feature);
						}
					}
				});
			}

			private void configureVisibility(Jackson2ObjectMapperBuilder builder,
					Map<PropertyAccessor, JsonAutoDetect.Visibility> visibilities) {
				visibilities.forEach(builder::visibility);
			}

			private void configureDateFormat(Jackson2ObjectMapperBuilder builder) {
				// We support a fully qualified class name extending DateFormat or a date
				// pattern string value
				String dateFormat = this.jacksonProperties.getDateFormat();
				if (dateFormat != null) {
					try {
						Class<?> dateFormatClass = ClassUtils.forName(dateFormat, null);
						builder.dateFormat(
								(DateFormat) BeanUtils.instantiateClass(dateFormatClass));
					}
					catch (ClassNotFoundException ex) {
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
								dateFormat);
						// Since Jackson 2.6.3 we always need to set a TimeZone (see
						// gh-4170). If none in our properties fallback to the Jackson's
						// default
						TimeZone timeZone = this.jacksonProperties.getTimeZone();
						if (timeZone == null) {
							timeZone = new ObjectMapper().getSerializationConfig()
									.getTimeZone();
						}
						simpleDateFormat.setTimeZone(timeZone);
						builder.dateFormat(simpleDateFormat);
					}
				}
			}

			private void configurePropertyNamingStrategy(
					Jackson2ObjectMapperBuilder builder) {
				// We support a fully qualified class name extending Jackson's
				// PropertyNamingStrategy or a string value corresponding to the constant
				// names in PropertyNamingStrategy which hold default provided
				// implementations
				String strategy = this.jacksonProperties.getPropertyNamingStrategy();
				if (strategy != null) {
					try {
						configurePropertyNamingStrategyClass(builder,
								ClassUtils.forName(strategy, null));
					}
					catch (ClassNotFoundException ex) {
						configurePropertyNamingStrategyField(builder, strategy);
					}
				}
			}

			private void configurePropertyNamingStrategyClass(
					Jackson2ObjectMapperBuilder builder,
					Class<?> propertyNamingStrategyClass) {
				builder.propertyNamingStrategy((PropertyNamingStrategy) BeanUtils
						.instantiateClass(propertyNamingStrategyClass));
			}

			private void configurePropertyNamingStrategyField(
					Jackson2ObjectMapperBuilder builder, String fieldName) {
				// Find the field (this way we automatically support new constants
				// that may be added by Jackson in the future)
				Field field = ReflectionUtils.findField(PropertyNamingStrategy.class,
						fieldName, PropertyNamingStrategy.class);
				Assert.notNull(field, () -> "Constant named '" + fieldName
						+ "' not found on " + PropertyNamingStrategy.class.getName());
				try {
					builder.propertyNamingStrategy(
							(PropertyNamingStrategy) field.get(null));
				}
				catch (Exception ex) {
					throw new IllegalStateException(ex);
				}
			}

			private void configureModules(Jackson2ObjectMapperBuilder builder) {
				Collection<com.fasterxml.jackson.databind.Module> moduleBeans = getBeans(
						this.applicationContext,
						com.fasterxml.jackson.databind.Module.class);
				builder.modulesToInstall(moduleBeans
						.toArray(new com.fasterxml.jackson.databind.Module[0]));
			}

			private void configureLocale(Jackson2ObjectMapperBuilder builder) {
				Locale locale = this.jacksonProperties.getLocale();
				if (locale != null) {
					builder.locale(locale);
				}
			}

			private static <T> Collection<T> getBeans(ListableBeanFactory beanFactory,
					Class<T> type) {
				return BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, type)
						.values();
			}

		}

	}
}
