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

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy.Default;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.TypeCreation;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.collection.ArrayFactory;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.utility.CompoundList;
import net.bytebuddy.utility.JavaConstant;
import plugin.conditions.ConditionalHandler;
import plugin.conditions.ConditionalOnClassHandler;
import plugin.conditions.ConditionalOnMissingBeanHandler;
import plugin.conditions.ConditionalOnPropertyHandler;
import plugin.conditions.FallbackConditionHandler;
import plugin.conditions.ProfileConditionHandler;
import plugin.custom.EnableFramesComputing;
import plugin.custom.InsertLabel;

// TODO [loose ends] ensure name is appropriate to avoid clashes

/**
 * Constructs the Initializer inner class. Something like this:
 * 
 * <pre>
 * <code>
 *  private static class Initializer implements ApplicationContextInitializer<GenericApplicationContext> {
 *        &#64;Override
 *        public void initialize(GenericApplicationContext context) {
 *            context.registerBean(SampleConfiguration.class);
 *            // All @Bean methods get registered like this:
 *            if (ClassUtils.isPresent("Wobble",null)) { // was @ConditionalOnClass(Wobble.class)
 *                context.registerBean("foo", Foo.class,
 *                        () -> context.getBean(SampleConfiguration.class).foo());
 *            }
 *            if (context.getBeanNamesForType(Bar.class).length == 0) { // was @ConditionalOnMissingBean where method returns Bar
 *                context.registerBean("bar", Bar.class, () -> context
 *                        .getBean(SampleConfiguration.class).bar(context.getBean(Foo.class)));
 *            }
 *            context.registerBean("runner", CommandLineRunner.class,
 *                    () -> context.getBean(SampleConfiguration.class)
 *                            .runner(context.getBean(Bar.class)));
 *        }
 * }
 * </code>
 * </pre>
 */
class InitializerClassFactory {

	private List<ConditionalHandler> conditionalHandlers = new ArrayList<>();
	private boolean needsConditionService;
	private FallbackConditionHandler fallbackConditionHandler;

	public InitializerClassFactory() {
		// Register condition 'optimizers'
		conditionalHandlers.add(new ConditionalOnClassHandler());
		conditionalHandlers.add(new ConditionalOnMissingBeanHandler());
		conditionalHandlers.add(new ProfileConditionHandler());
		conditionalHandlers.add(new ConditionalOnPropertyHandler());
		fallbackConditionHandler = new FallbackConditionHandler();
		needsConditionService = false;
	}

	public DynamicType make(TypeDescription configurationTypeDescription, String configurationInitializerTypeDescription, ClassFileLocator locator) throws Exception {
		log(":ifactory: Generating initializer class for " + configurationTypeDescription.getName()+" called "+configurationInitializerTypeDescription);
		DynamicType.Builder<?> builder = createBasicClassStructure(configurationInitializerTypeDescription);
		builder = addAnnotationInitializerMapping(configurationTypeDescription, builder);
		TypeDescription target = builder.make().getTypeDescription(); // crude
		builder = addConstructors(builder, target);
		builder = addMethodInitialize(configurationTypeDescription, builder, target);
		return builder.make();
	}

	private DynamicType.Builder<?> createBasicClassStructure(String configurationInitializerTypeDescription) {
		DynamicType.Builder<?> builder = new ByteBuddy()
				.subclass(Types.ParameterizedApplicationContextInitializerWithGenericApplicationContext(), Default.NO_CONSTRUCTORS)
				.visit(new EnableFramesComputing());
		builder = builder.modifiers(Modifier.STATIC).name(configurationInitializerTypeDescription);
		return builder;
	}

	private DynamicType.Builder<?> addMethodInitialize(TypeDescription configurationTypeDescription, DynamicType.Builder<?> builder, TypeDescription target) {
		List<StackManipulation> code = new ArrayList<>();
		builder = generateCodeForInitializeMethod(configurationTypeDescription, builder, target, code, true);
		builder = builder.method(named("initialize").and(isDeclaredBy(Types.ApplicationContextInitializer())))
				.intercept(new Implementation.Simple(new ByteCodeAppender.Simple(code)));
		return builder;
	}

	private Builder<?> addConstructors(Builder<?> builder, TypeDescription target) throws Exception {
		// TODO [bytebuddy] is there a ByteBuddy strategy for doing what javac does for private inner classes?
		// TODO [bytebuddy] why extra unnecessary bytecode in the generated ctor? (see extraneous DUP/POP)
		// Copy javac: create package private constructor visible from @Configuration type
		builder = addPackagePrivateConstructor(builder, target);
		// Make the no-arg default constructor private
		builder = addPrivateConstructor(builder);
		return builder;
	}

	private ReceiverTypeDefinition<?> addPrivateConstructor(DynamicType.Builder<?> builder) throws NoSuchMethodException {
		return builder.defineConstructor(Visibility.PRIVATE).intercept(MethodCall.invoke(Object.class.getDeclaredConstructor()));
	}

	private ReceiverTypeDefinition<?> addPackagePrivateConstructor(DynamicType.Builder<?> builder, TypeDescription target) throws NoSuchMethodException {
		return builder.defineConstructor(Visibility.PACKAGE_PRIVATE).withParameter(target)
				.intercept(MethodCall.invoke(Object.class.getDeclaredConstructor()));
	}

	/**
	 * Add @InitializerMapping(configurationTypeDescription).
	 */
	private Builder<?> addAnnotationInitializerMapping(TypeDescription configurationTypeDescription, DynamicType.Builder<?> builder) {
		return builder.annotateType(AnnotationDescription.Builder.ofType(Types.InitializerMapping())
				.defineTypeArray("value", new TypeDescription[] { configurationTypeDescription }).build());
	}

	private DynamicType.Builder<?> generateCodeForInitializeMethod(TypeDescription configurationTypeDescription, 
			DynamicType.Builder<?> builder, TypeDescription target, List<StackManipulation> code, boolean isOutermost) {
		
		log(":ifactory: generating functional registration code for "+configurationTypeDescription.getName());
		Label typeConditionsFailJumpTarget = new Label();
		code.addAll(Common.generateCodeToPrintln(":debug: Processing type level conditions on " + configurationTypeDescription.getName()));
		processConfigurationTypeLevelConditions(configurationTypeDescription, fallbackConditionHandler, code, typeConditionsFailJumpTarget);
		code.addAll(Common.generateCodeToPrintln(":debug: Passed condition checks on " + configurationTypeDescription.getName()));

		if (isOutermost) {
			// Store a reusable empty array of BeanDefinitionCustomizer
			code.add(ArrayFactory.forType(Types.BeanDefinitionCustomizer().asGenericType()).withValues(Collections.emptyList()));
			code.add(MethodVariableAccess.REFERENCE.storeAt(2));
		}

		// @EnableConfigurationProperties(GsonProperties.class) transfers into:
		// context.registerBean(GsonProperties.class, () -> new GsonProperties());
		AnnotationDescription enableConfigurationProperties = Common.findAnnotation(configurationTypeDescription, Types.EnableConfigurationProperties());
		if (enableConfigurationProperties != null) {
			log(":ifactory: Processing @EnableConfigurationProperties");
			TypeDescription[] types = Common.fetchTypeDescriptions(enableConfigurationProperties, "value");
			for (TypeDescription type : types) {
				// TODO [loose ends] need to get the right ctor, not the first one
				MethodDescription.InDefinedShape ctor = type.getDeclaredMethods().filter((em) -> em.isConstructor()).get(0);
				List<StackManipulation> insns = new ArrayList<>();
				insns.add(TypeCreation.of(type));
				insns.add(Duplication.SINGLE);
				insns.add(MethodInvocation.invoke(ctor));
				insns.add(MethodReturn.of(type));
				String tdname = type.getActualName();
				if (tdname.indexOf(".") != -1) {
					tdname = tdname.substring(tdname.lastIndexOf(".") + 1);
				}
				String supplierLambdaName = "init_" + tdname;
				builder = builder.defineMethod(supplierLambdaName, type.asErasure(), Visibility.PRIVATE, Ownership.STATIC).withParameters(Types.BeanFactory())
						.intercept(new Implementation.Simple(new ByteCodeAppender.Simple(insns)));
				code.addAll(createRegisterBeanCodeForECP(target, type, supplierLambdaName));
			}
		}

		// TODO: mark the bean definition somehow so it doesn't get processed by ConfigurationClassPostProcessor
		// TODO [loose ends] avoid reflection at runtime by finding the right constructor now and generating the code
		// to call it.
//		if (Common.hasNoArgConstructor(configurationTypeDescription)) {
			log(":ifactory: registering bean "+configurationTypeDescription+" directly");
			// Call context.registerBean(SampleConfiguration.class)
			code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
			code.add(ClassConstant.of(configurationTypeDescription));
			code.add(MethodVariableAccess.REFERENCE.loadFrom(2));
			code.add(MethodInvocation.invoke(Methods.registerBean()));
//		} else {
//			log(":ifactory: registering bean "+configurationTypeDescription+" via supplier (due to no simple constructor)");
//			// Call context.registerBean(SampleConfiguration.class,()-> new SampleConfiguration(context.getBean(thingyNeeded))
//		}

		// deal with the situation where you can't register the type because it has a non trivial constructor, see EnableWebFluxConfiguration

			// TODO [loose ends] look at generics here, not erasure of supers
		log(":ifactory: Processing @Bean methods...");
		
		TypeDescription td = configurationTypeDescription;
		List<String> alreadyProcessed = new ArrayList<>();
		while (td != null) {
			log("  :ifactory: Looking for @Bean methods on "+td.getName());
			for (MethodDescription.InDefinedShape methodDescription : td.getDeclaredMethods().filter(isAnnotatedWith(Types.Bean()))) {
				log("  :ifactory: Processing @Bean method "+methodDescription);
				if (alreadyProcessed.contains(methodDescription.getName())) {
					// TODO [loose ends] need to consider parameters
					continue;
				}
				alreadyProcessed.add(methodDescription.getName());
				ParameterList<net.bytebuddy.description.method.ParameterDescription.InDefinedShape> parameters = methodDescription.getParameters();
				TypeList parameterErasures = parameters.asTypeList().asErasures();
				List<StackManipulation> stackManipulations = new ArrayList<>();
				stackManipulations.addAll(Common.generateCodeToPrintln(":debug: execution of lambda for " + methodDescription.getName()));
				List<TypeDescription> parameterErasures2 = methodDescription.isStatic() ? parameterErasures
						: CompoundList.of(configurationTypeDescription, parameterErasures);
				for (int a = 0; a < parameterErasures2.size(); a++) {
					TypeDescription argumentType = parameterErasures2.get(a);
					// Computation of this parameter may depend on the bean param, is it a collection?
					if (argumentType.equals(Types.ApplicationContext())) { // was represents(ApplicationContext.class)) {
						// Example: See Jackson2ObjectMapperBuilderCustomizerConfiguration - the @Bean
						// factory method takes one
						stackManipulations.add(MethodVariableAccess.REFERENCE.loadFrom(0));
						stackManipulations.add(TypeCasting.to(Types.ApplicationContext()));
					} else if (argumentType.equals(Types.List())) {
						TypeDescription collectionTypeParameterDescriptor = parameters.asTypeList().get(methodDescription.isStatic() ? a : a - 1).getTypeArguments()
								.get(0).asErasure();
						stackManipulations.add(TypeCreation.of(new TypeDescription.ForLoadedType(ArrayList.class)));
						stackManipulations.add(Duplication.SINGLE);
						stackManipulations.add(MethodVariableAccess.REFERENCE.loadFrom(0));
						stackManipulations.add(TypeCasting.to(Types.AbstractApplicationContext()));//new TypeDescription.ForLoadedType(AbstractApplicationContext.class)));
						stackManipulations.add(ClassConstant.of(collectionTypeParameterDescriptor));
						stackManipulations.add(MethodInvocation.invoke(Methods.getBeansOfType()));
						stackManipulations.add(MethodInvocation.invoke(Methods.mapValues()));
						stackManipulations.add(MethodInvocation.invoke(Methods.arraylistCtor()));
	//				         9: new           #93                 // class java/util/ArrayList
	//				         12: dup
	//				         13: aload_0
	//				         14: ldc           #73                 // class org/springframework/boot/autoconfigure/gson/GsonBuilderCustomizer
	
	//				        16: invokevirtual #95                 // Method org/springframework/context/support/GenericApplicationContext.getBeansOfType:(Ljava/lang/Class;)Ljava/util/Map;
	//				        19: invokeinterface #99,  1           // InterfaceMethod java/util/Map.values:()Ljava/util/Collection;
	//				        24: invokespecial #105                // Method java/util/ArrayList."<init>":(Ljava/util/Collection;)V
					} else {
						// Call BeanFactory.getBean(Class)
						stackManipulations.add(MethodVariableAccess.REFERENCE.loadFrom(0));
						stackManipulations.add(ClassConstant.of(argumentType));
						stackManipulations.add(MethodInvocation.invoke(Methods.getBean()));
					}
	
					stackManipulations.add(TypeCasting.to(argumentType));
				}
				stackManipulations.add(MethodInvocation.invoke(methodDescription));
				stackManipulations.add(MethodReturn.of(methodDescription.getReturnType()));
	
				log("  :ifactory: Creating lambda method for "+methodDescription);
				builder = builder
						.defineMethod("init_" + methodDescription.getName(), methodDescription.getReturnType().asErasure(), Visibility.PRIVATE, Ownership.STATIC)
						.withParameters(Types.BeanFactory()).intercept(new Implementation.Simple(new ByteCodeAppender.Simple(stackManipulations)));
	
				code.addAll(createRegisterBeanCode(target, methodDescription));
			}
			// TODO [loose ends] have to worry about default methods in interfaces?
			td = td.getSuperClass().asErasure();
			if (td.equals(Types.Object())) {
				break;
			}
		}

		// Look at the inner types of the auto configuration class
		TypeList memberTypes = configurationTypeDescription.getDeclaredTypes();
		log(":ifactory: Processing inner type of "+configurationTypeDescription);
		for (TypeDescription memberType : memberTypes) {
			boolean b = false;
			try {
				b = Common.hasAnnotation(memberType, Types.Configuration());
			} catch (Throwable t) {
				throw new IllegalStateException("Problem checking hasAnnotation() on " + memberType.getName(), t);
			}
			if (b) {
				log(":ifactory: generating functional registration code for member type " + memberType.getName());
				builder = generateCodeForInitializeMethod(memberType, builder, target, code, false);
			}
		}

		code.add(new InsertLabel(typeConditionsFailJumpTarget));
		if (isOutermost) {
			code.add(MethodReturn.VOID);
		}

		if (needsConditionService && isOutermost) {
			log(":ifactory: Creating fallback condition service (required due to some conditions used in configuration)");
			TypeDescription conditionServiceTypeDescription = Types.ConditionService();
			code.add(0, MethodVariableAccess.REFERENCE.loadFrom(1));
			code.add(1, MethodInvocation.invoke(Methods.getBeanFactory()));
			code.add(2, ClassConstant.of(conditionServiceTypeDescription));
			code.add(3, MethodInvocation.invoke(Methods.getBean()));
			code.add(4, TypeCasting.to(conditionServiceTypeDescription));
			code.add(5, MethodVariableAccess.REFERENCE.storeAt(3));
		}
		return builder;
	}

	private void processConfigurationTypeLevelConditions(TypeDescription configurationTypeDescription, FallbackConditionHandler fallbackConditionHandler,
			List<StackManipulation> code, Label typeConditionsFailJumpTarget) {
		// Process the conditions on the this type and any outer types
		List<AnnotationDescription> conditionalAnnotations = fetchConditionalAnnotations(configurationTypeDescription);
		if (conditionalAnnotations.size() == 0) {
			log("No type level conditions on " + configurationTypeDescription);
		} else {
			log("Applying the following type level conditions to " + configurationTypeDescription + ": " + conditionalAnnotations);
		}
		// Check if fallback needed for any of these... if so, use fallback to check
		// all of them
		boolean fallbackRequired = false;
		for (AnnotationDescription annoDescription : conditionalAnnotations) {
			boolean handled = false;
			for (ConditionalHandler handler : conditionalHandlers) {
				if (handler.accept(annoDescription)) {
					handled = true;
				}
			}
			if (!handled) {
				log("Due to existence of " + annoDescription + " on " + configurationTypeDescription
						+ " the fallback handler is being used for all conditions on the type");
				fallbackRequired = true;
				needsConditionService = true;
			}
		}
		if (fallbackRequired) {
			code.addAll(fallbackConditionHandler.computeStackManipulations(null, configurationTypeDescription, typeConditionsFailJumpTarget));
		} else {
			for (AnnotationDescription annoDescription : conditionalAnnotations) {
				for (ConditionalHandler handler : conditionalHandlers) {
					if (handler.accept(annoDescription)) {
						code.addAll(handler.computeStackManipulations(annoDescription, configurationTypeDescription, typeConditionsFailJumpTarget));
					}
				}
			}
		}
	}

	public DynamicType make(TypeDescription configurationTypeDescription, ClassFileLocator locator) throws Exception {
		String initializerTypeName = configurationTypeDescription.getTypeName() + "$Initializer";
		return make(configurationTypeDescription, initializerTypeName, locator);
	}

	// TODO sort out correct logging...
	private void log(String message) {
		System.out.println(message);
	}

	// For EnableConfigurationProperties
	private List<StackManipulation> createRegisterBeanCodeForECP(TypeDescription initializerType, TypeDescription td, String supplierLambdaName) {
		List<StackManipulation> code = new ArrayList<>();
		code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
		code.add(ClassConstant.of(td.asErasure()));
		code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
		MethodDescription.InDefinedShape lambda = new MethodDescription.Latent(initializerType, supplierLambdaName, Modifier.PRIVATE | Modifier.STATIC,
				Collections.emptyList(), td.asGenericType(),
				Collections.singletonList(new ParameterDescription.Token(Types.BeanFactory().asGenericType())),
				Collections.emptyList(), Collections.emptyList(), null, null);
		code.add(MethodInvocation.invoke(Methods.metafactory()).dynamic("get", new TypeDescription.ForLoadedType(Supplier.class),
				Collections.singletonList(Types.BeanFactory()),
				Arrays.asList(JavaConstant.MethodType.of(Methods.get()).asConstantPoolValue(), JavaConstant.MethodHandle.of(lambda).asConstantPoolValue(),
						JavaConstant.MethodType.of(td.asErasure(), Collections.emptyList()).asConstantPoolValue())));
		code.add(ArrayFactory.forType(Types.BeanDefinitionCustomizer().asGenericType()).withValues(Collections.emptyList()));
		code.add(MethodInvocation.invoke(Methods.registerBeanWithSupplier()));
		return code;
	}

	private List<StackManipulation> createRegisterBeanCode(TypeDescription initializerType, MethodDescription.InDefinedShape methodDescription) {
		List<StackManipulation> code = new ArrayList<>();
		code.addAll(Common.generateCodeToPrintln(":debug: Checking conditions before calling registerBean for bean factory method " + methodDescription.getName()));
		// TODO would be better to create the registerBean code than wrap it recursively in condition checks
		AnnotationList conditionalAnnotations = fetchConditionalAnnotations(methodDescription);
		Label conditionsFailJumpTarget = new Label();
		boolean fallbackRequired = false;
		for (AnnotationDescription annoDescription : conditionalAnnotations) {
			boolean handled = false;
			for (ConditionalHandler handler : conditionalHandlers) {
				if (handler.accept(annoDescription)) {
					handled = true;
					break;
				}
			}
			if (!handled) {
				log("Due to existence of " + annoDescription + " on " + methodDescription
						+ " the fallback handler is being used for all conditions for this method");
				fallbackRequired = true;
			}
		}
		if (fallbackRequired) {
			needsConditionService = true;
			code.addAll(fallbackConditionHandler.computeStackManipulations(null, methodDescription, conditionsFailJumpTarget));
		} else {
			for (AnnotationDescription annoDescription : conditionalAnnotations) {
				for (ConditionalHandler handler : conditionalHandlers) {
					if (handler.accept(annoDescription)) {
						code.addAll(handler.computeStackManipulations(annoDescription, methodDescription, conditionsFailJumpTarget));
					}
				}
			}
		}
		code.addAll(Common.generateCodeToPrintln(":debug: Calling registerBean for bean factory method " + methodDescription.getName()));
		// Create code to call registerBean
		code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
		code.add(new TextConstant(methodDescription.getName()));
		code.add(ClassConstant.of(methodDescription.getReturnType().asErasure()));
		code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
		MethodDescription.InDefinedShape lambda = new MethodDescription.Latent(initializerType, "init_" + methodDescription.getName(),
				Modifier.PRIVATE | Modifier.STATIC, Collections.emptyList(), methodDescription.getReturnType().asRawType(),
				Collections.singletonList(new ParameterDescription.Token(Types.BeanFactory().asGenericType())),
				Collections.emptyList(), Collections.emptyList(), null, null);
		code.add(MethodInvocation.invoke(Methods.metafactory()).dynamic("get", new TypeDescription.ForLoadedType(Supplier.class),
				Collections.singletonList(Types.BeanFactory()),
				Arrays.asList(JavaConstant.MethodType.of(Methods.get()).asConstantPoolValue(), JavaConstant.MethodHandle.of(lambda).asConstantPoolValue(),
						JavaConstant.MethodType.of(methodDescription.getReturnType().asErasure(), Collections.emptyList()).asConstantPoolValue())));
		code.add(ArrayFactory.forType(Types.BeanDefinitionCustomizer().asGenericType()).withValues(Collections.emptyList()));
		code.add(MethodInvocation.invoke(Methods.registerBeanWithSupplierIncludingName()));

		code.add(new InsertLabel(conditionsFailJumpTarget));
		return code;
	}

	private AnnotationList fetchConditionalAnnotations(InDefinedShape methodDescription) {
		AnnotationList list = methodDescription.getDeclaredAnnotations().filter(this::isConditionalAnnotation);
		return list;
	}

	private List<AnnotationDescription> fetchConditionalAnnotations(TypeDescription typeDescription) {
		List<AnnotationDescription> result = new ArrayList<>();
		AnnotationList list = null;
		try {
			list = typeDescription.getDeclaredAnnotations().filter(this::isConditionalAnnotation);
		} catch (Throwable t) {
			throw new RuntimeException("Can't check annotations on " + typeDescription.getActualName(), t);
		}
		for (AnnotationDescription ad : list) {
			result.add(ad);
		}
//			while (typeDescription.isNestedClass()) {
//				typeDescription = typeDescription.getEnclosingType();
//				// System.out.println("Collecting conditions from outer class
//				// "+typeDescription);
//				list = typeDescription.getDeclaredAnnotations().filter(this::isConditionalAnnotation);
//				for (AnnotationDescription ad : list) {
//					result.add(ad);
//				}
//			}
		return result;
	}

	public boolean isConditionalAnnotation(AnnotationDescription annoDescription) {
		return isAnnotated(annoDescription, Types.Conditional(), new HashSet<>());
	}

	private boolean isAnnotated(AnnotationDescription desc, TypeDescription annotationClass, Set<AnnotationDescription> seen) {
		seen.add(desc);
		TypeDescription type = desc.getAnnotationType();
		if (type.equals(annotationClass)) {
			return true;
		}
		for (AnnotationDescription ann : type.getDeclaredAnnotations()) {
			if (!seen.contains(ann) && isAnnotated(ann, annotationClass, seen)) {
				return true;
			}
		}
		return false;
	}

}
