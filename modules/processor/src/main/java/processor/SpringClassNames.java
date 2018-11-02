/*
 * Copyright 2016-2017 the original author or authors.
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
package processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;

/**
 * @author Dave Syer
 *
 */
public class SpringClassNames {

	public static final ClassName CONFIGURATION = ClassName
			.get("org.springframework.context.annotation", "Configuration");

	public static final ClassName SPRING_BOOT_CONFIGURATION = ClassName
			.get("org.springframework.boot", "SpringBootConfiguration");

	public static final ClassName BEAN = ClassName
			.get("org.springframework.context.annotation", "Bean");

	public static final ClassName IMPORT = ClassName
			.get("org.springframework.context.annotation", "Import");

	public static final ClassName INITIALIZER_MAPPING = ClassName.get("slim",
			"InitializerMapping");

	public static final ClassName CONDITION_SERVICE = ClassName.get("slim",
			"ConditionService");

	public static final ClassName MODULE = ClassName.get("slim", "Module");

	public static final ClassName APPLICATION_CONTEXT_INITIALIZER = ClassName
			.get("org.springframework.context", "ApplicationContextInitializer");

	public static final ClassName GENERIC_APPLICATION_CONTEXT = ClassName
			.get("org.springframework.context.support", "GenericApplicationContext");

	public static final ParameterizedTypeName INITIALIZER_TYPE = ParameterizedTypeName
			.get(APPLICATION_CONTEXT_INITIALIZER, GENERIC_APPLICATION_CONTEXT);

	public static final ClassName CONDITIONAL = ClassName
			.get("org.springframework.context.annotation", "Conditional");
	
	public static final ClassName ENABLE_CONFIGURATION_PROPERTIES = ClassName
			.get("org.springframework.boot.context.properties", "EnableConfigurationProperties");

}
