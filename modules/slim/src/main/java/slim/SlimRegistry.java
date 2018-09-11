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

package slim;

import java.util.function.Supplier;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.lang.Nullable;

/**
 * @author Dave Syer
 *
 */
public class SlimRegistry {

	public static <T> void register(BeanDefinitionRegistry context, String name,
			Class<T> type, Supplier<T> supplier,
			BeanDefinitionCustomizer... customizers) {
		context.registerBeanDefinition(name, bean(type, supplier, customizers));
	}

	public static <T> void register(BeanDefinitionRegistry context, String name,
			Class<T> type, BeanDefinitionCustomizer... customizers) {
		context.registerBeanDefinition(name, bean(type, null, customizers));
	}

	public static <T> void register(BeanDefinitionRegistry context, Class<T> type,
			Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {
		context.registerBeanDefinition(type.getName(), bean(type, supplier, customizers));
	}

	public static <T> void register(BeanDefinitionRegistry context, Class<T> type,
			BeanDefinitionCustomizer... customizers) {
		context.registerBeanDefinition(type.getName(), bean(type, null, customizers));
	}

	private static <T> BeanDefinition bean(Class<T> beanClass,
			@Nullable Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {
		BeanDefinitionBuilder builder = (supplier != null
				? BeanDefinitionBuilder.genericBeanDefinition(beanClass, supplier)
				: BeanDefinitionBuilder.genericBeanDefinition(beanClass));
		return builder.applyCustomizers(customizers).getRawBeanDefinition();
	}

}
