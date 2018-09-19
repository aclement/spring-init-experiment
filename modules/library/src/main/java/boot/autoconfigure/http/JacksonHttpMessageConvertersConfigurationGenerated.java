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

package boot.autoconfigure.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

import boot.autoconfigure.http.JacksonHttpMessageConvertersConfigurationGenerated.JacksonHttpMessageConvertersConfiguration.MappingJackson2HttpMessageConverterConfiguration;
import boot.autoconfigure.http.JacksonHttpMessageConvertersConfigurationGenerated.JacksonHttpMessageConvertersConfiguration.MappingJackson2XmlHttpMessageConverterConfiguration;
import slim.ConditionService;
import slim.InitializerMapping;
import slim.SlimConfiguration;

/**
 * @author Dave Syer
 *
 */
@SlimConfiguration
public class JacksonHttpMessageConvertersConfigurationGenerated {

	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return new Initializer();
	}

	@InitializerMapping(HttpMessageConvertersAutoConfiguration.class)
	static class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			ConditionService conditions = context.getBeanFactory()
					.getBean(ConditionService.class);
			if (conditions
					.matches(MappingJackson2HttpMessageConverterConfiguration.class)) {
				if (conditions.matches(
						MappingJackson2HttpMessageConverterConfiguration.class,
						MappingJackson2HttpMessageConverter.class)) {
					context.registerBean(MappingJackson2HttpMessageConverterConfiguration.class);
					context.registerBean(MappingJackson2HttpMessageConverter.class,
							() -> context.getBean(
									MappingJackson2HttpMessageConverterConfiguration.class)
									.mappingJackson2HttpMessageConverter(
											context.getBean(ObjectMapper.class)));
				}
			}
			if (conditions
					.matches(MappingJackson2XmlHttpMessageConverterConfiguration.class)) {
				if (conditions.matches(
						MappingJackson2XmlHttpMessageConverterConfiguration.class,
						MappingJackson2XmlHttpMessageConverter.class)) {
					context.registerBean(MappingJackson2XmlHttpMessageConverterConfiguration.class);
					context.registerBean(MappingJackson2XmlHttpMessageConverter.class,
							() -> context.getBean(
									MappingJackson2XmlHttpMessageConverterConfiguration.class)
									.mappingJackson2XmlHttpMessageConverter(context
											.getBean(Jackson2ObjectMapperBuilder.class)));
				}
			}
		}

	}

	// Copied from Spring Boot (package private)
	@Configuration
	static class JacksonHttpMessageConvertersConfiguration {

		static final String PREFERRED_MAPPER_PROPERTY = "spring.http.converters.preferred-json-mapper";

		@Configuration
		@ConditionalOnClass(ObjectMapper.class)
		@ConditionalOnBean(ObjectMapper.class)
		@ConditionalOnProperty(name = PREFERRED_MAPPER_PROPERTY, havingValue = "jackson", matchIfMissing = true)
		protected static class MappingJackson2HttpMessageConverterConfiguration {

			@Bean
			@ConditionalOnMissingBean(value = MappingJackson2HttpMessageConverter.class, ignoredType = {
					"org.springframework.hateoas.mvc.TypeConstrainedMappingJackson2HttpMessageConverter",
					"org.springframework.data.rest.webmvc.alps.AlpsJsonHttpMessageConverter" })
			public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
					ObjectMapper objectMapper) {
				return new MappingJackson2HttpMessageConverter(objectMapper);
			}

		}

		@Configuration
		@ConditionalOnClass(name = "org.springframework.boot.autoconfigure.http.XmlMapper")
		@ConditionalOnBean(Jackson2ObjectMapperBuilder.class)
		protected static class MappingJackson2XmlHttpMessageConverterConfiguration {

			@Bean
			@ConditionalOnMissingBean
			public MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter(
					Jackson2ObjectMapperBuilder builder) {
				return new MappingJackson2XmlHttpMessageConverter(
						builder.createXmlMapper(true).build());
			}

		}

	}

}
