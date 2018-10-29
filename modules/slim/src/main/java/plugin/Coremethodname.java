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

/**
 * 
 * @author Andy Clement
 */
public enum Coremethodname {

	getBeanFactory("getBeanFactory"), // On GenericApplicationContext
	registerBean("registerBean"), // On GenericApplicationContext
	registerBeanWithSupplier("registerBean"), 
	getBean("getBean"), // BeanFactory.getBean(Class)
	arraylistCtor("java.util.ArrayList"), // ArrayList.init(Collection)
	getBeansOfType("getBeansOfType"), // AbstractApplicationContext.getBeansOfType(Class)
	mapValues("values"), // Map.values()
	isPresent("isPresent"), // ClassUtils.isPresent(String,ClassLoader)
	matches("matches"), // ConditionService.matches(Class)
	matches2("matches"), // ConditionService.matches(Class,Class)
	getEnvironment("getEnvironment"), // GenericApplicationContext.getEnvironment()
	of("of"), // Profiles.of(String[])
	acceptsProfiles("acceptsProfiles"), // Environment.acceptsProfiles(Profiles)
	get("get"), // Supplier.get()
	containsProperty("containsProperty"), // PropertyResolver.containsProperty(String)
	getBeanNamesForType("getBeanNamesForType"), // ConfigurableListableBeanFactory.getBeanNamesForType(Class)
	metafactory("metafactory"), // LambdaMetaFactory.metafactory(MethodHandles.Lookup, String, MethodType, MethodType, MethodHandle, MethodType);
	////lambdaMeta = new MethodDescription.ForLoadedMethod(LambdaMetafactory.class.getMethod("metafactory", MethodHandles.Lookup.class, String.class,
////MethodType.class, MethodType.class, MethodHandle.class, MethodType.class));

	;
	
	private String fqname;

	Coremethodname(String fqname) {
		this.fqname = fqname;
	}
	
	public String getName() {
		return this.fqname;
	}
}