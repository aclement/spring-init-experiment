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

package org.springframework.boot.autoconfigure.http.codec;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpProperties;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.MimeType;

import slim.ConditionService;

/**
 * @author Dave Syer
 *
 */
public class CodecsAutoConfigurationGenerated {
	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return new Initializer();
	}

	static class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			ConditionService conditions = context.getBeanFactory()
					.getBean(ConditionService.class);
			if (conditions.matches(CodecsAutoConfiguration.class)) {
				if (conditions.matches(JacksonCodecConfiguration.class)) {
					if (conditions.matches(JacksonCodecConfiguration.class,
							CodecCustomizer.class)) {
						context.registerBean(JacksonCodecConfiguration.class);
						context.registerBean("jacksonCodecCustomizer",
								CodecCustomizer.class,
								() -> context.getBean(JacksonCodecConfiguration.class)
										.jacksonCodecCustomizer(
												context.getBean(ObjectMapper.class)));
					}
				}
				if (conditions.matches(LoggingCodecConfiguration.class)) {
					if (conditions.matches(LoggingCodecConfiguration.class,
							CodecCustomizer.class)) {
						context.registerBean(LoggingCodecConfiguration.class);
						context.registerBean("loggingCodecCustomizer",
								CodecCustomizer.class,
								() -> context.getBean(LoggingCodecConfiguration.class)
										.loggingCodecCustomizer(
												context.getBean(HttpProperties.class)));
					}
				}
			}
		}
	}

	// Copied from Spring Boot
	private static final MimeType[] EMPTY_MIME_TYPES = {};

	@Configuration
	@ConditionalOnClass(ObjectMapper.class)
	static class JacksonCodecConfiguration {

		@Bean
		@ConditionalOnBean(ObjectMapper.class)
		public CodecCustomizer jacksonCodecCustomizer(ObjectMapper objectMapper) {
			return (configurer) -> {
				CodecConfigurer.DefaultCodecs defaults = configurer.defaultCodecs();
				defaults.jackson2JsonDecoder(
						new Jackson2JsonDecoder(objectMapper, EMPTY_MIME_TYPES));
				defaults.jackson2JsonEncoder(
						new Jackson2JsonEncoder(objectMapper, EMPTY_MIME_TYPES));
			};
		}

	}

	@Configuration
	@EnableConfigurationProperties(HttpProperties.class)
	static class LoggingCodecConfiguration {

		@Bean
		public CodecCustomizer loggingCodecCustomizer(HttpProperties properties) {
			return (configurer) -> configurer.defaultCodecs()
					.enableLoggingRequestDetails(properties.isLogRequestDetails());
		}

	}
}
