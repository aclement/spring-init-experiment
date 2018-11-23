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

package org.springframework.boot.autoconfigure.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.http.JacksonHttpMessageConvertersConfiguration.MappingJackson2HttpMessageConverterConfiguration;
import org.springframework.boot.autoconfigure.http.JacksonHttpMessageConvertersConfiguration.MappingJackson2XmlHttpMessageConverterConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

import slim.ConditionService;

/**
 * @author Dave Syer
 *
 */
public class JacksonHttpMessageConvertersConfigurationGenerated {

	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return new Initializer();
	}

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
					context.registerBean(
							MappingJackson2HttpMessageConverterConfiguration.class);
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
					context.registerBean(
							MappingJackson2XmlHttpMessageConverterConfiguration.class);
					context.registerBean(MappingJackson2XmlHttpMessageConverter.class,
							() -> context.getBean(
									MappingJackson2XmlHttpMessageConverterConfiguration.class)
									.mappingJackson2XmlHttpMessageConverter(context
											.getBean(Jackson2ObjectMapperBuilder.class)));
				}
			}
		}

	}

}
