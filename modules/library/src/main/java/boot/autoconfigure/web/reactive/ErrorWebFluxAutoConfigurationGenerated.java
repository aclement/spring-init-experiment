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

package boot.autoconfigure.web.reactive;

import java.util.List;

import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import slim.ConditionService;
import slim.InitializerMapping;
import slim.ImportModule;

/**
 * @author Dave Syer
 *
 */
@ImportModule
class ErrorWebFluxAutoConfigurationGenerated {

	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return new Initializer();
	}

	@InitializerMapping(ErrorWebFluxAutoConfiguration.class)
	private static final class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			ConditionService conditions = context.getBeanFactory()
					.getBean(ConditionService.class);
			if (conditions.matches(ErrorWebFluxAutoConfiguration.class)) {
				context.registerBean(ErrorAttributes.class,
						() -> new DefaultErrorAttributes(
								context.getBean(ServerProperties.class).getError()
										.isIncludeException()));
				context.registerBean(ErrorWebExceptionHandler.class, () -> {
					return errorWebFluxAutoConfiguration(context)
							.errorWebExceptionHandler(
									context.getBean(ErrorAttributes.class));
				});
			}
		}

		private ErrorWebFluxAutoConfiguration errorWebFluxAutoConfiguration(
				GenericApplicationContext context) {
			ServerProperties serverProperties = context.getBean(ServerProperties.class);
			ResourceProperties resourceProperties = context
					.getBean(ResourceProperties.class);
			ServerCodecConfigurer serverCodecs = context
					.getBean(ServerCodecConfigurer.class);
			return new ErrorWebFluxAutoConfiguration(serverProperties, resourceProperties,
					context.getDefaultListableBeanFactory()
							.getBeanProvider(ResolvableType.forClassWithGenerics(
									List.class, ViewResolver.class)),
					serverCodecs, context);
		}

	}

}
