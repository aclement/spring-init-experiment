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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.TemplateLoader;

import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.boot.autoconfigure.mustache.MustacheProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;

import boot.autoconfigure.context.ContextAutoConfigurationModule;
import slim.ConditionService;
import slim.Module;

/**
 * @author Dave Syer
 *
 */
@Configuration
@Import({ MustacheAutoConfiguration.class, ContextAutoConfigurationModule.class })
public class MustacheAutoConfigurationModule implements Module {

	@Override
	public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
		// Why do I need to do this?
		if (!ClassUtils.isPresent("com.samskivert.mustache.Mustache", null)) {
			return Collections.emptyList();
		}
		return Arrays.asList(new Initializer(),
				MustacheReactiveWebConfigurationGenerated.initializer());
	}

	static class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			ConditionService conditions = context.getBeanFactory()
					.getBean(ConditionService.class);
			if (conditions.matches(MustacheAutoConfiguration.class)) {
				context.registerBean(MustacheProperties.class,
						() -> new MustacheProperties());
				context.registerBean(MustacheAutoConfiguration.class);
				if (conditions.matches(MustacheAutoConfiguration.class, Compiler.class)) {
					context.registerBean(Compiler.class,
							() -> context.getBean(MustacheAutoConfiguration.class)
									.mustacheCompiler(
											context.getBean(TemplateLoader.class)));
				}
				if (conditions.matches(MustacheAutoConfiguration.class,
						TemplateLoader.class)) {
					context.registerBean(TemplateLoader.class,
							() -> context.getBean(MustacheAutoConfiguration.class)
									.mustacheTemplateLoader());
				}
			}
		}

	}

}
