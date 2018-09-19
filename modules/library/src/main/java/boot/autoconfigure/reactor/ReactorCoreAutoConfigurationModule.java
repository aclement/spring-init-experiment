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

package boot.autoconfigure.reactor;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.reactor.core.ReactorCoreAutoConfiguration;
import org.springframework.boot.autoconfigure.reactor.core.ReactorCoreProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import slim.ConditionService;
import slim.InitializerMapping;
import slim.Module;
import slim.ModuleMapping;

/**
 * @author Dave Syer
 *
 */
@ModuleMapping(ReactorCoreAutoConfiguration.class)
public class ReactorCoreAutoConfigurationModule implements Module {

	@Override
	public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
		return Arrays.asList(new Initializer());
	}

	@InitializerMapping(ReactorCoreAutoConfiguration.class)
	static class Initializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			ConditionService conditions = context.getBeanFactory()
					.getBean(ConditionService.class);
			if (conditions.matches(ReactorCoreAutoConfiguration.class)) {
				context.registerBean(ReactorCoreProperties.class,
						() -> new ReactorCoreProperties());
				context.registerBean(ReactorCoreAutoConfiguration.class,
						() -> new ReactorCoreAutoConfiguration());
			}
		}

	}

}
