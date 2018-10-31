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
import java.util.List;
import java.util.Map;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;

/**
 * 
 * @author Andy Clement
 */
public class Methods {
	
	public final static Map<Coremethodname, MethodDescription.InDefinedShape> methods = new HashMap<>();
	
	public static MethodDescription.InDefinedShape get(Coremethodname name) {
		try {
			InDefinedShape result = methods.get(name);
			if (result == null) {
				switch (name) {
				case metafactory: 
					result = findMethod(Types.LambdaMetaFactory(), Coremethodname.metafactory,
							Types.MethodHandlesLookup(), Types.String(), Types.MethodType(), Types.MethodType(),
							Types.MethodHandle(), Types.MethodType());
					break;
				case getBeanNamesForType:
					result = findMethod(Types.ListableBeanFactory(), Coremethodname.getBeanNamesForType, Types.Class());
					break;
				case containsProperty:
					result = findMethod(Types.PropertyResolver(), Coremethodname.containsProperty,Types.String());
					break;
				case get:
					result = findMethod(Types.Supplier(), Coremethodname.get);
					break;
				case acceptsProfiles:
					result = findMethod(Types.Environment(),Coremethodname.acceptsProfiles, Types.Profiles());
					break;
				case of:
					result = findMethod(Types.Profiles(), Coremethodname.of, Types.StringArray());
					break;
				case getEnvironment:
					result = findMethod(Types.ConfigurableApplicationContext(), Coremethodname.getEnvironment);
					break;
				case matches2:
					result = findMethod(Types.ConditionService(), Coremethodname.matches, Types.Class(),Types.Class());
					break;
				case matches:
					result = findMethod(Types.ConditionService(), Coremethodname.matches, Types.Class());
					break;
				case isPresent:
					result = findMethod(Types.ClassUtils(), Coremethodname.isPresent,Types.String(),Types.ClassLoader());
					break;
				case mapValues:
					result = findMethod(Types.Map(), Coremethodname.mapValues);
					break;
				case getBeansOfType:
					result = findMethod(Types.AbstractApplicationContext(), Coremethodname.getBeansOfType, Types.Class());
					break;
				case arraylistCtor:
					result = findMethod(Types.ArrayList(), Coremethodname.arraylistCtor, Types.Collection());
					break;
				case getBean:
					result = findMethod(Types.BeanFactory(), Coremethodname.getBean, Types.Class());
					break;
				case getBeanFactory:
					result = findMethod(Types.GenericApplicationContext(),Coremethodname.getBeanFactory);
					break;
				case registerBean:
					result = findMethod(Types.GenericApplicationContext(), Coremethodname.registerBean, Types.Class(), Types.BeanDefinitionCustomizerArray());
					break;
				case registerBeanWithSupplier:
					result = findMethod(Types.GenericApplicationContext(), Coremethodname.registerBeanWithSupplier, Types.Class(),Types.Supplier(), Types.BeanDefinitionCustomizerArray());
					break;
				case registerBeanWithSupplierIncludingName:
					result = findMethod(Types.GenericApplicationContext(), Coremethodname.registerBeanWithSupplier, Types.String(), Types.Class(),Types.Supplier(), Types.BeanDefinitionCustomizerArray());
					break;
				default:
					throw new IllegalStateException(name.toString());
				}
				methods.put(name, result);
			}
			if (result == null ) {
				throw new RuntimeException("Why is this method not found? "+name);
			}
			return result;
		} catch (SecurityException e) {
			throw new IllegalStateException("Expected to be able to resolve method: "+name);
		}
	}

	public static MethodDescription.InDefinedShape findMethod(TypeDescription td, Coremethodname methodname, TypeDefinition... parameters) {
		return findMethod(td, methodname.getName(), parameters);
	}
	
	public static MethodDescription.InDefinedShape findMethod(TypeDescription td, String methodname, TypeDefinition... parameters) {
		MethodList<?> mds = td.getDeclaredMethods();
		for (MethodDescription md : mds) {
			if (md.getName().equals(methodname)) {
				List<TypeDescription> parameterTypes = md.asSignatureToken().getParameterTypes();
				if (parameterTypes.size() != parameters.length) {
					continue;
				}
				boolean paramsMatch = true;
				for (int i=0;i<parameters.length;i++) {
					if (!parameterTypes.get(i).equals(parameters[i])) {
						paramsMatch = false;
					}
					break;
				}
				if (paramsMatch) {
					return md.asDefined();
				}
			}
		}	
		return null;
	}

	public static void _verify() {
		getBeanFactory();
		registerBean();
	}

	// ---

	public static InDefinedShape getBeanFactory() {
		return Methods.get(Coremethodname.getBeanFactory);
	}
	
	public static InDefinedShape registerBean() {
		return Methods.get(Coremethodname.registerBean);
	}
	
	public static InDefinedShape registerBeanWithSupplier() {
		return Methods.get(Coremethodname.registerBeanWithSupplier);
	}
	
	public static InDefinedShape registerBeanWithSupplierIncludingName() {
		return Methods.get(Coremethodname.registerBeanWithSupplierIncludingName);
	}

	public static InDefinedShape getBean() {
		return Methods.get(Coremethodname.getBean);
	}

	public static InDefinedShape getBeansOfType() {
		return Methods.get(Coremethodname.getBeansOfType);
	}
	
	public static InDefinedShape arraylistCtor() {
		return Methods.get(Coremethodname.arraylistCtor);
	}
	
	public static InDefinedShape mapValues() {
		return Methods.get(Coremethodname.mapValues);
	}

	public static InDefinedShape isPresent() {
		return Methods.get(Coremethodname.isPresent);
	}

	public static InDefinedShape matches() {
		return Methods.get(Coremethodname.matches);
	}

	public static InDefinedShape matches2() {
		return Methods.get(Coremethodname.matches2);
	}

	public static InDefinedShape getEnvironment() {
		return Methods.get(Coremethodname.getEnvironment);
	}

	public static InDefinedShape of() {
		return Methods.get(Coremethodname.of);
	}

	public static InDefinedShape acceptsProfiles() {
		return Methods.get(Coremethodname.acceptsProfiles);
	}
	
	public static InDefinedShape get() {
		return Methods.get(Coremethodname.get);
	}

	public static InDefinedShape containsProperty() {
		return Methods.get(Coremethodname.containsProperty);
	}

	public static InDefinedShape getBeanNamesForType() {
		return Methods.get(Coremethodname.getBeanNamesForType);
	}
	
	public static InDefinedShape metafactory() {
		return Methods.get(Coremethodname.metafactory);
	}
	
	public static void main(String[] args) {
		Methods.verify();
	}

	public static void verify() {
		for (Coremethodname t: Coremethodname.values()) {
			Methods.get(t);
		}
	}
	
}
