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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.collection.ArrayFactory;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.jar.asm.Opcodes;

class ModuleClassFactory {

		private void log(String message) {
			System.out.println(message);
		}

		public ModuleClassFactory() {
		}

		private String toModuleName(String typename) {
			if (typename.endsWith("Module")) {
				return typename; // nothing to do for now
			}
			if (typename.endsWith("Configuration")) {
				return typename.substring(0, typename.indexOf("Configuration")) + "Module";
			} else if (typename.endsWith("Application")) {
				return typename.substring(0, typename.indexOf("Application")) + "Module";
			}
			return typename + "Module";
		}

		public DynamicType make(TypeDescription typeDescription, ClassFileLocator locator,
				TypeDescription[] typesWithInitializeMethods) throws Exception {
			return make(typeDescription, null, locator, null, typesWithInitializeMethods, null);
		}

		public DynamicType make(TypeDescription typeDescription, String autoConfigurationClass,
				ClassFileLocator locator, DynamicType initializerClassType,
				TypeDescription[] typesWithInitializeMethods,
				TypeDescription[] initializersForOtherImportedConfigurations) throws Exception {
			log("Generating module for " + typeDescription.getName() + " calling");

			if (typesWithInitializeMethods == null || typesWithInitializeMethods.length == 0) {
				log("NOTHING");
			} else {
				for (TypeDescription td : typesWithInitializeMethods) {
					log("- " + td);
				}
			}
			String moduleName = toModuleName(typeDescription.getTypeName());
			DynamicType.Builder<?> builder = new ByteBuddy().subclass(Types.Module()).name(moduleName);
			log("Module: " + moduleName);

			builder = addAtConfigurationAnnotation(builder);

			if (autoConfigurationClass != null) {
				// @Import(GsonAutoConfiguration.class)
				builder = builder.annotateType(AnnotationDescription.Builder.ofType(Types.Import())
						.defineTypeArray("value",
								// TypeDescription newModule = new TypeDescription.Latent(moduleName,
								// Opcodes.ACC_PUBLIC,
								// TypeDescription.Generic.OBJECT);
								// TODO yuck - don't like creating a fake version (latent) of the real class
								// that looks like it (supertypes)
								new TypeDescription[] { new TypeDescription.Latent(autoConfigurationClass,
										Opcodes.ACC_PUBLIC, TypeDescription.Generic.OBJECT) })
						.build());
			}

			// @ImportModule(module = ContextAutoConfigurationModule.class)
			// TODO: [needs proper solution]
			if (moduleName.endsWith("GsonAutoConfigurationModule")
					|| moduleName.endsWith("MustacheAutoConfigurationModule")
					|| moduleName.endsWith("JacksonAutoConfigurationModule")) {
				builder = builder.annotateType(AnnotationDescription.Builder.ofType(Types.ImportModule())
						.defineTypeArray("module",
								new TypeDescription[] { new TypeDescription.Latent(
										"boot.autoconfigure.context.ContextAutoConfigurationModule", Opcodes.ACC_PUBLIC,
										TypeDescription.Generic.OBJECT,
										Types.Module().asGenericType()) })
						.build());
			}

			Generic Type_ACI = TypeDescription.Generic.Builder.rawType(Types.ApplicationContextInitializer()).build();

			List<StackManipulation> code = new ArrayList<>();
			List<StackManipulation> eachElement = new ArrayList<>();
			code.addAll(Common.generateCodeToPrintln(":debug: executing the module initializers() method for " + moduleName));

			Generic Type_ListOfACI = TypeDescription.Generic.Builder
					.parameterizedType(new TypeDescription.ForLoadedType(List.class),
							Types.ParameterizedApplicationContextInitializerWithGenericApplicationContext())
					.build();

			if (typesWithInitializeMethods != null) {
				for (int i = 0; i < typesWithInitializeMethods.length; i++) {
					TypeDescription td = typesWithInitializeMethods[i];
					MethodDescription md = new MethodDescription.Latent(td, // declaringType,
							"$$initializer", // internalName,
							Modifier.PUBLIC | Modifier.STATIC, // modifiers,
							Collections.emptyList(), // typeVariables,
							Type_ACI, // returnType,
							Collections.emptyList(), // parameterTokens,
							Collections.emptyList(), // exceptionTypes,
							null, // declaredAnnotations,
							null, // defaultValue,
							null); // receiverType)
					eachElement.add(MethodInvocation.invoke(md));
				}
			}

			if (initializerClassType != null) {
				MethodDescription md = new MethodDescription.Latent(typeDescription,
						// initializerClassType.getTypeDescription(), // declaringType,
						"$$initializer", // internalName,
						Modifier.PUBLIC | Modifier.STATIC, // modifiers,
						Collections.emptyList(), // typeVariables,
						Type_ACI, // returnType,
						Collections.emptyList(), // parameterTokens,
						Collections.emptyList(), // exceptionTypes,
						null, // declaredAnnotations,
						null, // defaultValue,
						null); // receiverType)
				eachElement.add(MethodInvocation.invoke(md));

			}

			if (initializersForOtherImportedConfigurations != null) {
				log(moduleName + " initializer for other imported configuration: #"
						+ initializersForOtherImportedConfigurations.length);
				for (TypeDescription td : initializersForOtherImportedConfigurations) {
					log(moduleName + " initializer for other imported configuration: " + td.getName());
					MethodDescription md = new MethodDescription.Latent(typeDescription,
							// initializerClassType.getTypeDescription(), // declaringType,
							toShortName(td.getName()), // internalName,
							Modifier.PUBLIC | Modifier.STATIC, // modifiers,
							Collections.emptyList(), // typeVariables,
							Type_ACI, // returnType,
							Collections.emptyList(), // parameterTokens,
							Collections.emptyList(), // exceptionTypes,
							null, // declaredAnnotations,
							null, // defaultValue,
							null); // receiverType)
					eachElement.add(MethodInvocation.invoke(md));
				}
			}

			code.add(ArrayFactory
					.forType(Types.ApplicationContextInitializer().asGenericType())
					.withValues(eachElement));

			code.add(MethodInvocation
					.invoke(new MethodDescription.ForLoadedMethod(Arrays.class.getMethod("asList", Object[].class))));
			code.add(MethodReturn.of(Types.ParameterizedApplicationContextInitializerWithGenericApplicationContext()));

			builder = builder.defineMethod("initializers", Type_ListOfACI, Modifier.PUBLIC)
					.intercept(new Implementation.Simple(new ByteCodeAppender.Simple(code)));

			if (initializerClassType != null) {
				builder = Common.addInitializerMethod(builder, initializerClassType);
			} else {
				log(":debug: ?? no initializer class type ??");
			}

			if (initializersForOtherImportedConfigurations != null) {
				for (TypeDescription td : initializersForOtherImportedConfigurations) {
					builder = Common.addInitializerMethod(builder, td, toShortName(td.getName()));
				}
			}

			return builder.make();
		}

		private String toShortName(String s) {
//			org.springframework.boot.autoconfigure.gson.MustacheAutoConfigurationModule$MustacheReactiveWebConfiguration_Initializer
			return "$$" + s.substring(s.lastIndexOf("$") + 1);
//			System.out.println("<><><>"+s);
//			return s;
		}

		private Builder<?> addAtConfigurationAnnotation(DynamicType.Builder<?> builder) {
			return builder.annotateType(AnnotationDescription.Builder.ofType(Types.Configuration()).build());
		}

	}