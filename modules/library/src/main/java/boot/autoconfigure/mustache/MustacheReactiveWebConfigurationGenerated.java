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

package boot.autoconfigure.mustache;

import com.samskivert.mustache.Mustache.Compiler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.boot.autoconfigure.mustache.MustacheProperties;
import org.springframework.boot.web.reactive.result.view.MustacheViewResolver;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;

import boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfigurationModule;
import slim.ConditionService;
import slim.InitializerMapping;
import slim.SlimConfiguration;

/**
 * @author Dave Syer
 *
 */
// Generated from MustacheReactiveWebConfiguration
@SlimConfiguration(module = { MustacheAutoConfigurationModule.class,
		ReactiveWebServerFactoryAutoConfigurationModule.class })
public class MustacheReactiveWebConfigurationGenerated {

	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return new Initializer();
	}

	@InitializerMapping(MustacheAutoConfiguration.class)
	static class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			ConditionService conditions = context.getBeanFactory()
					.getBean(ConditionService.class);
			if (conditions.matches(MustacheReactiveWebConfiguration.class)) {
				context.registerBean(MustacheReactiveWebConfiguration.class);
				if (conditions.matches(MustacheReactiveWebConfiguration.class,
						MustacheViewResolver.class)) {
					context.registerBean(MustacheViewResolver.class,
							() -> context.getBean(MustacheReactiveWebConfiguration.class)
									.mustacheViewResolver(
											context.getBean(Compiler.class)));
				}
			}
		}

	}

	// Copy of MustacheReactiveWebConfiguration package private class in Boot
	@Configuration
	@ConditionalOnWebApplication(type = Type.REACTIVE)
	static class MustacheReactiveWebConfiguration {
		private final MustacheProperties mustache;

		protected MustacheReactiveWebConfiguration(MustacheProperties mustache) {
			this.mustache = mustache;
		}

		@Bean
		@ConditionalOnMissingBean
		public MustacheViewResolver mustacheViewResolver(Compiler mustacheCompiler) {
			MustacheViewResolver resolver = new MustacheViewResolver(mustacheCompiler);
			resolver.setPrefix(this.mustache.getPrefix());
			resolver.setSuffix(this.mustache.getSuffix());
			resolver.setViewNames(this.mustache.getViewNames());
			resolver.setRequestContextAttribute(
					this.mustache.getRequestContextAttribute());
			resolver.setCharset(this.mustache.getCharsetName());
			resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
			return resolver;
		}
	}

}
