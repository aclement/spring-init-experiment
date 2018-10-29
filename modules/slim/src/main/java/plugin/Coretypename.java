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
public enum Coretypename {
	Class("java.lang.Class"),
	Supplier("java.util.function.Supplier"),
	PropertyResolver("org.springframework.core.env.PropertyResolver"),
	ApplicationContextInitializer("org.springframework.context.ApplicationContextInitializer"),
	GenericApplicationContext("org.springframework.context.support.GenericApplicationContext"),
	BeanDefinitionCustomizer("org.springframework.beans.factory.config.BeanDefinitionCustomizer"),
	BeanFactory("org.springframework.beans.factory.BeanFactory"),
	Import("org.springframework.context.annotation.Import"),
	Bean("org.springframework.context.annotation.Bean"),
	Conditional("org.springframework.context.annotation.Conditional"),
	Configuration("org.springframework.context.annotation.Configuration"),
	EnableConfigurationProperties("org.springframework.boot.context.properties.EnableConfigurationProperties"),
	ApplicationContext("org.springframework.context.ApplicationContext"),
	AbstractApplicationContext("org.springframework.context.support.AbstractApplicationContext"), 
	Map("java.util.Map"),
	Collection("java.util.Collection"),
	ArrayList("java.util.ArrayList"),
	List("java.util.List"),
	ImportModule("slim.ImportModule"),
	Module("slim.Module"),
	MethodHandle("java.lang.invoke.MethodHandle"),
	MethodType("java.lang.invoke.MethodType"),
	LambdaMetafactory("java.lang.invoke.LambdaMetafactory"),
	MethodHandlesLookup("java.lang.invoke.MethodHandles$Lookup"),
	ConfigurableListableBeanFactory("org.springframework.beans.factory.config.ConfigurableListableBeanFactory"),
	ListableBeanFactory("org.springframework.beans.factory.ListableBeanFactory"),
	InitializerMapping("slim.InitializerMapping"),
	ConfigurableApplicationContext("org.springframework.context.ConfigurableApplicationContext"),
	Environment("org.springframework.core.env.Environment"),
	String("java.lang.String"),
	Profiles("org.springframework.core.env.Profiles"),
	SpringBootConfiguration("org.springframework.boot.SpringBootConfiguration"),
	ClassLoader("java.lang.ClassLoader"),
	ConditionService("slim.ConditionService"),
	ClassUtils("org.springframework.util.ClassUtils"),
	Profile("org.springframework.context.annotation.Profile"),
	ConditionalOnProperty("org.springframework.boot.autoconfigure.condition.ConditionalOnProperty"),
	ConditionalOnClass("org.springframework.boot.autoconfigure.condition.ConditionalOnClass"),
	ConditionalOnMissingBean("org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean"),
	;
	
	private String fqname;

	Coretypename(String fqname) {
		this.fqname = fqname;
	}
	
	public String getName() {
		return this.fqname;
	}
}