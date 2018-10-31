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
package plugin;

import java.util.HashMap;
import java.util.Map;

import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.ArrayProjection;
import net.bytebuddy.description.type.TypeDescription.Generic;

/**
 * 
 * @author Andy Clement
 */
public class Types {
	
	private static Map<String,Class<?>> loadedTypes = new HashMap<>();
	private static Map<String,TypeDescription> loadedTypeDescriptions = new HashMap<>();
	
	public static Class<?> get(String name) {
		Class<?> result = loadedTypes.get(name);
		if (result == null) {
			result = resolve(name);
			loadedTypes.put(name, result);
		}
		return result;
	}

	private static Class<?> resolve(String name) {
		try {
			return Class.forName(name,false,Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException cnfe) {
			throw new IllegalStateException("Couldn't find "+name,cnfe);
		}
	}
	
	public static TypeDescription getTypeDescription(String name) {
		// TODO is this double caching? (see of() method below)
		TypeDescription result = loadedTypeDescriptions.get(name);
		if (result == null) {
			result = TypeDescription.ForLoadedType.of(get(name));
			loadedTypeDescriptions.put(name, result);
		}
		return result;
	}

	public static TypeDescription getTypeDescription(Coretypename typename) {
		return getTypeDescription(typename.getName());
	}
	
	
	public static void verify() {
		// Core types must be resolvable!
		for (Coretypename t: Coretypename.values()) {
			try {
				resolve(t.getName());
			} catch (Throwable t2) {
				throw new IllegalStateException("Unablet to resolve "+t.getName(),t2);
			}
		}
	}
	
	// ---
	
	public static TypeDescription ApplicationContextInitializer() {
		return getTypeDescription(Coretypename.ApplicationContextInitializer);
	}

	public static TypeDescription GenericApplicationContext() {
		return getTypeDescription(Coretypename.GenericApplicationContext);
	}
	
	public static TypeDescription BeanDefinitionCustomizer() {
		return getTypeDescription(Coretypename.BeanDefinitionCustomizer);
	}

	private static Generic ParameterizedApplicationContextInitializerWithGenericApplicationContext;
	
	public static TypeDefinition ParameterizedApplicationContextInitializerWithGenericApplicationContext() {
		if (ParameterizedApplicationContextInitializerWithGenericApplicationContext == null) {
			ParameterizedApplicationContextInitializerWithGenericApplicationContext = TypeDescription.Generic.Builder
				.parameterizedType(Types.ApplicationContextInitializer(),Types.GenericApplicationContext())
				.build();
		}
		return ParameterizedApplicationContextInitializerWithGenericApplicationContext;
	}

	public static TypeDescription Class() {
		return getTypeDescription(Coretypename.Class);
	}
	
	public static TypeDescription BeanDefinitionCustomizerArray() {
		// TODO cache
		return ArrayProjection.of(BeanDefinitionCustomizer());
	}

	public static TypeDescription Supplier() {
		return getTypeDescription(Coretypename.Supplier);
	}
	
	public static TypeDescription PropertyResolver() {
		return getTypeDescription(Coretypename.PropertyResolver);
	}

	public static TypeDescription BeanFactory() {
		return getTypeDescription(Coretypename.BeanFactory);
	}

	public static TypeDescription Import() {
		return getTypeDescription(Coretypename.Import);
	}

	public static TypeDescription ApplicationContext() {
		return getTypeDescription(Coretypename.ApplicationContext);
	}

	public static TypeDescription AbstractApplicationContext() {
		return getTypeDescription(Coretypename.AbstractApplicationContext);
	}

	public static TypeDescription Map() {
		return getTypeDescription(Coretypename.Map);
	}
	
	public static TypeDescription Collection() {
		return getTypeDescription(Coretypename.Collection);
	}

	public static TypeDescription ArrayList() {
		return getTypeDescription(Coretypename.ArrayList);
	}

	public static TypeDescription ImportModule() {
		return getTypeDescription(Coretypename.ImportModule);
	}

	public static TypeDescription Module() {
		return getTypeDescription(Coretypename.Module);
	}

	public static TypeDescription ConditionalOnProperty() {
		return getTypeDescription(Coretypename.ConditionalOnProperty);
	}

	public static TypeDescription Configuration() {
		return getTypeDescription(Coretypename.Configuration);
	}

	public static TypeDescription Profile() {
		return getTypeDescription(Coretypename.Profile);
	}

	public static TypeDescription ConditionalOnClass() {
		return getTypeDescription(Coretypename.ConditionalOnClass);
	}

	public static TypeDescription ConditionalOnMissingBean() {
		return getTypeDescription(Coretypename.ConditionalOnMissingBean);
	}

	public static TypeDescription Bean() {
		return getTypeDescription(Coretypename.Bean);
	}

	public static TypeDescription String() {
		return getTypeDescription(Coretypename.String);
	}

	public static TypeDescription ClassUtils() {
		return getTypeDescription(Coretypename.ClassUtils);
	}
	
	public static TypeDescription ClassLoader() {
		return getTypeDescription(Coretypename.ClassLoader);
	}

	public static TypeDescription ConditionService() {
		return getTypeDescription(Coretypename.ConditionService);
	}

	public static TypeDescription SpringBootConfiguration() {
		return getTypeDescription(Coretypename.SpringBootConfiguration);
	}

	public static TypeDescription Profiles() {
		return getTypeDescription(Coretypename.Profiles);
	}
	
	public static TypeDescription StringArray() {
		return ArrayProjection.of(String());
	}

	public static TypeDescription EnableConfigurationProperties() {
		return getTypeDescription(Coretypename.EnableConfigurationProperties);
	}

	public static TypeDescription Conditional() {
		return getTypeDescription(Coretypename.Conditional);
	}

	public static TypeDescription Environment() {
		return getTypeDescription(Coretypename.Environment);
	}

	public static TypeDescription InitializerMapping() {
		return getTypeDescription(Coretypename.InitializerMapping);
	}

	public static TypeDescription ConfigurableListableBeanFactory() {
		return getTypeDescription(Coretypename.ConfigurableListableBeanFactory);
	}

	public static TypeDefinition MethodHandle() {
		return getTypeDescription(Coretypename.MethodHandle);
	}
	
	public static TypeDefinition MethodType() {
		return getTypeDescription(Coretypename.MethodType);
	}

	public static TypeDescription LambdaMetaFactory() {
		return getTypeDescription(Coretypename.LambdaMetafactory);
	}

	public static TypeDefinition MethodHandlesLookup() {
		return getTypeDescription(Coretypename.MethodHandlesLookup);
	}

	public static TypeDescription ConfigurableApplicationContext() {
		return getTypeDescription(Coretypename.ConfigurableApplicationContext);
	}

	public static TypeDescription ListableBeanFactory() {
		return getTypeDescription(Coretypename.ListableBeanFactory);
	}

	public static Object List() {
		return getTypeDescription(Coretypename.List);
	}

	public static Object Object() {
		return getTypeDescription(Coretypename.Object);
	}

}
