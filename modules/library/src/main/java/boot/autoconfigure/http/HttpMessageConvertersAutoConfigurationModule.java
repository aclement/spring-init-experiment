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

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.converter.StringHttpMessageConverter;

import boot.autoconfigure.context.ContextAutoConfigurationModule;
import boot.autoconfigure.gson.GsonAutoConfigurationModule;
import boot.autoconfigure.http.codec.CodecsAutoConfigurationGenerated;
import slim.ConditionService;
import slim.Module;
import slim.SlimConfiguration;

/**
 * @author Dave Syer
 *
 */
// TODO: add Jackson and Jsonb
@SlimConfiguration(module = { ContextAutoConfigurationModule.class,
		GsonAutoConfigurationModule.class })
public class HttpMessageConvertersAutoConfigurationModule implements Module {

	@Override
	public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
		return Arrays.asList(new Initializer(),
				GsonHttpMessageConvertersConfigurationGenerated.initializer(),
				JacksonHttpMessageConvertersConfigurationGenerated.initializer(),
				CodecsAutoConfigurationGenerated.initializer());
	}

	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return new Initializer();
	}

	static class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			ConditionService conditions = context.getBeanFactory()
					.getBean(ConditionService.class);
			if (conditions.matches(HttpMessageConvertersAutoConfiguration.class)) {
				context.registerBean(HttpMessageConvertersAutoConfiguration.class);
				context.registerBean(HttpProperties.class, () -> new HttpProperties());
				if (conditions.matches(HttpMessageConvertersAutoConfiguration.class,
						HttpMessageConverters.class)) {
					context.registerBean(HttpMessageConverters.class,
							() -> context
									.getBean(HttpMessageConvertersAutoConfiguration.class)
									.messageConverters());
				}
				// TODO: if this was nested in HttpMessageConvertersAutoConfiguration it
				// could refer to the protected nested class there instead of manually
				// creating these beans
				context.registerBean(StringHttpMessageConverter.class,
						() -> stringHttpMessageConverter(context));
			}
		}

		StringHttpMessageConverter stringHttpMessageConverter(
				GenericApplicationContext context) {
			StringHttpMessageConverter converter = new StringHttpMessageConverter(
					context.getBean(HttpProperties.class).getEncoding().getCharset());
			converter.setWriteAcceptCharset(false);
			return converter;
		}

	}

}
