//package plugin.asm;
//
//public class InitializerClassFactory {
//
//	private final MethodDescription.InDefinedShape registerBean, registerBeanWithSupplier, getBean, lambdaMeta, get,
//			getBeanFactory, getBeansOfType, map_values, arraylist_ctor;
//	private List<ConditionalHandler> conditionalHandlers = new ArrayList<>();
//	private boolean needsConditionService;
//	private FallbackConditionHandler fallbackConditionHandler;
//
//	private TypeDescription listTD;
//
//	public InitializerClassFactory() {
//		try {
//			getBeanFactory = new MethodDescription.ForLoadedMethod(
//					GenericApplicationContext.class.getMethod("getBeanFactory"));
//			registerBean = new MethodDescription.ForLoadedMethod(GenericApplicationContext.class
//					.getMethod("registerBean", Class.class, BeanDefinitionCustomizer[].class));
//			registerBeanWithSupplier = new MethodDescription.ForLoadedMethod(GenericApplicationContext.class
//					.getMethod("registerBean", Class.class, Supplier.class, BeanDefinitionCustomizer[].class));
//			getBean = new MethodDescription.ForLoadedMethod(BeanFactory.class.getMethod("getBean", Class.class));
//			arraylist_ctor = new MethodDescription.ForLoadedConstructor(
//					ArrayList.class.getConstructor(Collection.class));
//			getBeansOfType = new MethodDescription.ForLoadedMethod(
//					AbstractApplicationContext.class.getMethod("getBeansOfType", Class.class));
//			lambdaMeta = new MethodDescription.ForLoadedMethod(
//					LambdaMetafactory.class.getMethod("metafactory", MethodHandles.Lookup.class, String.class,
//							MethodType.class, MethodType.class, MethodHandle.class, MethodType.class));
//			map_values = new MethodDescription.ForLoadedMethod(java.util.Map.class.getMethod("values"));
//			get = new MethodDescription.ForLoadedMethod(Supplier.class.getMethod("get"));
//			listTD = new TypeDescription.ForLoadedType(java.util.List.class);
//		} catch (NoSuchMethodException e) {
//			throw new RuntimeException(e);
//		}
//		conditionalHandlers.add(new ConditionalOnClassHandler());
//		// results in :
////			java.lang.IllegalStateException: org.springframework.context.support.GenericApplicationContext@52719fb6 has not been refreshed yet
////			at org.springframework.context.support.AbstractApplicationContext.assertBeanFactoryActive(AbstractApplicationContext.java:1070) ~[spring-context-5.1.2.BUILD-SNAPSHOT.jar:5.1.2.BUILD-SNAPSHOT]
////			at org.springframework.context.support.AbstractApplicationContext.getBeanNamesForType(AbstractApplicationContext.java:1191) ~[sp
//		conditionalHandlers.add(new ConditionalOnMissingBeanHandler());
//		conditionalHandlers.add(new ProfileConditionHandler());
//		conditionalHandlers.add(new ConditionalOnPropertyHandler());
//		needsConditionService = false;
//		fallbackConditionHandler = new FallbackConditionHandler();
//	}
//
//	public DynamicType make(TypeDescription configurationTypeDescription, ClassFileLocator locator) throws Exception {
//		String initializerTypeName = configurationTypeDescription.getTypeName() + "$Initializer";
//		return make(configurationTypeDescription, initializerTypeName, locator);
//	}
//
//	public DynamicType make(TypeDescription configurationTypeDescription,
//			String configurationInitializerTypeDescription, ClassFileLocator locator) throws Exception {
//
//		DynamicType.Builder<?> builder = new ByteBuddy()
//				.subclass(Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext,
//						Default.NO_CONSTRUCTORS)
//				.visit(new EnableFramesComputing());
//
//		// TODO how to do logging from a bytebuddy plugin?
//		log("Generating initializer for " + configurationTypeDescription.getName());
//
//		builder = builder.modifiers(Modifier.STATIC).name(configurationInitializerTypeDescription);
//		builder = builder.annotateType(AnnotationDescription.Builder.ofType(InitializerMapping.class)
//				.defineTypeArray("value", new TypeDescription[] { configurationTypeDescription }).build());
//
//		TypeDescription target = builder.make().getTypeDescription();
//
//		// TODO is there a ByteBuddy strategy for doing what javac does for private
//		// inner classes?
//
//		// Copy javac: create package private constructor visible from @Configuration
//		// type
//		// TODO why extra unnecessary bytecode in the generated ctor? (see extraneous
//		// DUP/POP)
//		builder = builder.defineConstructor(Visibility.PACKAGE_PRIVATE).withParameter(target)
//				.intercept(MethodCall.invoke(Object.class.getDeclaredConstructor()));
//		// Make the default ctor private
//		builder = builder.defineConstructor(Visibility.PRIVATE)
//				.intercept(MethodCall.invoke(Object.class.getDeclaredConstructor()));
//
//		List<StackManipulation> code = new ArrayList<>();
//		Label typeConditionsFailJumpTarget = new Label();
//		code.addAll(getCodeToPrintln(
//				":debug: Processing type level conditions on " + configurationTypeDescription.getName()));
//		processConfigurationTypeLevelConditions(configurationTypeDescription, fallbackConditionHandler, code,
//				typeConditionsFailJumpTarget);
//		code.addAll(getCodeToPrintln(":debug: Passed condition checks on " + configurationTypeDescription.getName()));
//
//		// Store a reusable empty array of BeanDefinitionCustomizer
//		code.add(ArrayFactory.forType(new TypeDescription.ForLoadedType(BeanDefinitionCustomizer.class).asGenericType())
//				.withValues(Collections.emptyList()));
//		code.add(MethodVariableAccess.REFERENCE.storeAt(2));
//
//		// @EnableConfigurationProperties(GsonProperties.class) transfers into:
//		// context.registerBean(GsonProperties.class, () -> new GsonProperties());
//		AnnotationDescription enableConfigurationProperties = findAnnotation(configurationTypeDescription,
//				EnableConfigurationProperties.class);
//		if (enableConfigurationProperties != null) {
//			MethodList<MethodDescription.InDefinedShape> methodList = TypeDescription.ForLoadedType
//					.of(EnableConfigurationProperties.class).getDeclaredMethods();
//			InDefinedShape ECP_Value = methodList.filter(named("value")).getOnly();
//			TypeDescription[] types = (TypeDescription[]) enableConfigurationProperties.getValue(ECP_Value).resolve();
//			for (TypeDescription type : types) {
//				System.out.println(">>>>A>A>A>A>" + type.getActualName());
//				// TODO need to get the right ctor, not the first one
//				MethodDescription.InDefinedShape ctor = type.getDeclaredMethods().filter((em) -> em.isConstructor())
//						.get(0);
//				List<StackManipulation> insns = new ArrayList<>();
//				insns.add(TypeCreation.of(type));
//				insns.add(Duplication.SINGLE);
//				insns.add(MethodInvocation.invoke(ctor));
//				insns.add(MethodReturn.of(type));
//				String tdname = type.getActualName();
//				if (tdname.indexOf(".") != -1) {
//					tdname = tdname.substring(tdname.lastIndexOf(".") + 1);
//				}
//				String supplierLambdaName = "init_" + tdname;
//				builder = builder
//						.defineMethod(supplierLambdaName, type.asErasure(), Visibility.PRIVATE, Ownership.STATIC)
//						.withParameters(BeanFactory.class)
//						.intercept(new Implementation.Simple(new ByteCodeAppender.Simple(insns)));
//				code.addAll(createRegisterBeanCode2(target, type, supplierLambdaName));
//			}
//		}
//
//		// TODO: mark the bean definition somehow so it doesn't get
//		// processed by ConfigurationClassPostProcessor
//		// Call context.registerBean(SampleConfiguration.class)
//		code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
//		code.add(ClassConstant.of(configurationTypeDescription));
//		code.add(MethodVariableAccess.REFERENCE.loadFrom(2));
//		code.add(MethodInvocation.invoke(registerBean));
//
//		for (MethodDescription.InDefinedShape methodDescription : configurationTypeDescription.getDeclaredMethods()
//				.filter(isAnnotatedWith(Bean.class))) {
//
//			ParameterList<net.bytebuddy.description.method.ParameterDescription.InDefinedShape> parameters = methodDescription
//					.getParameters();
//			TypeList parameterErasures = parameters.asTypeList().asErasures();
//			List<StackManipulation> stackManipulations = new ArrayList<>();
//			stackManipulations
//					.addAll(getCodeToPrintln(":debug: execution of lambda for " + methodDescription.getName()));
//			List<TypeDescription> parameterErasures2 = methodDescription.isStatic() ? parameterErasures
//					: CompoundList.of(configurationTypeDescription, parameterErasures);
//			for (int a = 0; a < parameterErasures2.size(); a++) {
//				TypeDescription argumentType = parameterErasures2.get(a);
//				// Computation of this parameter may depend on the bean param, is it a
//				// collection?
//				if (argumentType.equals(listTD)) {
//					TypeDescription collectionTypeParameterDescriptor = parameters.asTypeList()
//							.get(methodDescription.isStatic() ? a : a - 1).getTypeArguments().get(0).asErasure();
//					stackManipulations.add(TypeCreation.of(new TypeDescription.ForLoadedType(ArrayList.class)));
//					stackManipulations.add(Duplication.SINGLE);
//					stackManipulations.add(MethodVariableAccess.REFERENCE.loadFrom(0));
//					stackManipulations
//							.add(TypeCasting.to(new TypeDescription.ForLoadedType(AbstractApplicationContext.class)));
//					stackManipulations.add(ClassConstant.of(collectionTypeParameterDescriptor));
//					stackManipulations.add(MethodInvocation.invoke(getBeansOfType));
//					stackManipulations.add(MethodInvocation.invoke(map_values));
//					stackManipulations.add(MethodInvocation.invoke(arraylist_ctor));
////				         9: new           #93                 // class java/util/ArrayList
////				         12: dup
////				         13: aload_0
////				         14: ldc           #73                 // class org/springframework/boot/autoconfigure/gson/GsonBuilderCustomizer
//
////				        16: invokevirtual #95                 // Method org/springframework/context/support/GenericApplicationContext.getBeansOfType:(Ljava/lang/Class;)Ljava/util/Map;
////				        19: invokeinterface #99,  1           // InterfaceMethod java/util/Map.values:()Ljava/util/Collection;
////				        24: invokespecial #105                // Method java/util/ArrayList."<init>":(Ljava/util/Collection;)V
//				} else {
//					// Call BeanFactory.getBean(Class)
//					stackManipulations.add(MethodVariableAccess.REFERENCE.loadFrom(0));
//					stackManipulations.add(ClassConstant.of(argumentType));
//					stackManipulations.add(MethodInvocation.invoke(getBean));
//				}
//
//				stackManipulations.add(TypeCasting.to(argumentType));
//			}
//			stackManipulations.add(MethodInvocation.invoke(methodDescription));
//			stackManipulations.add(MethodReturn.of(methodDescription.getReturnType()));
//
//			builder = builder
//					.defineMethod("init_" + methodDescription.getName(), methodDescription.getReturnType().asErasure(),
//							Visibility.PRIVATE, Ownership.STATIC)
//					.withParameters(BeanFactory.class)
//					.intercept(new Implementation.Simple(new ByteCodeAppender.Simple(stackManipulations)));
//
//			code.addAll(createRegisterBeanCode(target, methodDescription));
//		}
//
//		code.add(new InsertLabel(typeConditionsFailJumpTarget));
//		code.add(MethodReturn.VOID);
//
//		if (needsConditionService) {
//			TypeDescription conditionServiceTypeDescription = new TypeDescription.ForLoadedType(ConditionService.class);
//			code.add(0, MethodVariableAccess.REFERENCE.loadFrom(1));
//			code.add(1, MethodInvocation.invoke(getBeanFactory));
//			code.add(2, ClassConstant.of(conditionServiceTypeDescription));
//			code.add(3, MethodInvocation.invoke(getBean));
//			code.add(4, TypeCasting.to(conditionServiceTypeDescription));
//			code.add(5, MethodVariableAccess.REFERENCE.storeAt(3));
//		}
//
//		// Create the initialize() method
//		builder = builder.method(named("initialize").and(isDeclaredBy(ApplicationContextInitializer.class)))
//				.intercept(new Implementation.Simple(new ByteCodeAppender.Simple(code)));
//
//		return builder.make();
//	}
//
//	private void processConfigurationTypeLevelConditions(TypeDescription configurationTypeDescription,
//			FallbackConditionHandler fallbackConditionHandler, List<StackManipulation> code,
//			Label typeConditionsFailJumpTarget) {
//		// Process the conditions on the this type and any outer types
//		List<AnnotationDescription> conditionalAnnotations = fetchConditionalAnnotations(configurationTypeDescription);
//		if (conditionalAnnotations.size() == 0) {
//			log("No type level conditions on " + configurationTypeDescription);
//		} else {
//			log("Applying the following type level conditions to " + configurationTypeDescription + ": "
//					+ conditionalAnnotations);
//		}
//		// Check if fallback needed for any of these... if so, use fallback to check
//		// all of them
//		boolean fallbackRequired = false;
//		for (AnnotationDescription annoDescription : conditionalAnnotations) {
//			boolean handled = false;
//			for (ConditionalHandler handler : conditionalHandlers) {
//				if (handler.accept(annoDescription)) {
//					handled = true;
//				}
//			}
//			if (!handled) {
//				log("Due to existence of " + annoDescription + " on " + configurationTypeDescription
//						+ " the fallback handler is being used for all conditions on the type");
//				fallbackRequired = true;
//				needsConditionService = true;
//			}
//		}
//		if (fallbackRequired) {
//			code.addAll(fallbackConditionHandler.computeStackManipulations(null, configurationTypeDescription,
//					typeConditionsFailJumpTarget));
//		} else {
//			for (AnnotationDescription annoDescription : conditionalAnnotations) {
//				for (ConditionalHandler handler : conditionalHandlers) {
//					if (handler.accept(annoDescription)) {
//						code.addAll(handler.computeStackManipulations(annoDescription, configurationTypeDescription,
//								typeConditionsFailJumpTarget));
//					}
//				}
//			}
//		}
//	}
//
////		private TypeDescription getLatentType(String typename) {
////			new TypeDescription.Latent(autoConfigurationClass, Opcodes.ACC_PUBLIC,
////					TypeDescription.Generic.OBJECT) }
////		}
//
//	// For EnableConfigurationProperties
//	private List<StackManipulation> createRegisterBeanCode2(TypeDescription initializerType, TypeDescription td,
//			String supplierLambdaName) {
//		List<StackManipulation> code = new ArrayList<>();
//
//		// TODO would be better to create the registerBean code than wrap it
//		// recursively in condition checks
////			AnnotationList conditionalAnnotations = fetchConditionalAnnotations(methodDescription);
////			Label conditionsFailJumpTarget = new Label();
////			boolean fallbackRequired = false;
////			for (AnnotationDescription annoDescription : conditionalAnnotations) {
////				boolean handled = false;
////				for (ConditionalHandler handler : conditionalHandlers) {
////					if (handler.accept(annoDescription)) {
////						handled = true;
////						break;
////					}
////				}
////				if (!handled) {
////					log("Due to existence of " + annoDescription + " on " + methodDescription
////							+ " the fallback handler is being used for all conditions for this method");
////					fallbackRequired = true;
////				}
////			}
////			if (fallbackRequired) {
////				needsConditionService = true;
////				code.addAll(fallbackConditionHandler.computeStackManipulations(null, methodDescription,
////						conditionsFailJumpTarget));
////			} else {
////				for (AnnotationDescription annoDescription : conditionalAnnotations) {
////					for (ConditionalHandler handler : conditionalHandlers) {
////						if (handler.accept(annoDescription)) {
////							code.addAll(handler.computeStackManipulations(annoDescription, methodDescription,
////									conditionsFailJumpTarget));
////						}
////					}
////				}
////			}
////			net.bytebuddy.description.field.FieldDescription.InDefinedShape sysoutfield = new TypeDescription.ForLoadedType(System.class).getDeclaredFields().filter(em -> em.getActualName().equals("out")).get(0);
////			code.add(FieldAccess.STATIC.forField(sysoutfield).read());
////			code.add(new TextConstant("miracle"));
////			InDefinedShape printlnMethod = new TypeDescription.ForLoadedType(PrintStream.class).getDeclaredMethods().filter(em ->em.getActualName().equals("println")).get(0);
////			code.add(MethodInvocation.invoke(printlnMethod));
//
//		// Create code to call registerBean
//
////	         0: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
////	         3: ldc           #3                  // String aa
////	         5: invokevirtual #4                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
//
//		code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
//		code.add(ClassConstant.of(td.asErasure()));
//		code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
//		MethodDescription.InDefinedShape lambda = new MethodDescription.Latent(initializerType, supplierLambdaName,
//				Modifier.PRIVATE | Modifier.STATIC, Collections.emptyList(), td.asGenericType(),
//				Collections.singletonList(new ParameterDescription.Token(
//						new TypeDescription.ForLoadedType(BeanFactory.class).asGenericType())),
//				Collections.emptyList(), Collections.emptyList(), null, null);
//		code.add(MethodInvocation.invoke(lambdaMeta).dynamic("get", new TypeDescription.ForLoadedType(Supplier.class),
//				Collections.singletonList(new TypeDescription.ForLoadedType(BeanFactory.class)),
//				Arrays.asList(JavaConstant.MethodType.of(get).asConstantPoolValue(),
//						JavaConstant.MethodHandle.of(lambda).asConstantPoolValue(),
//						JavaConstant.MethodType.of(td.asErasure(), Collections.emptyList()).asConstantPoolValue())));
//		code.add(ArrayFactory.forType(new TypeDescription.ForLoadedType(BeanDefinitionCustomizer.class).asGenericType())
//				.withValues(Collections.emptyList()));
//		code.add(MethodInvocation.invoke(registerBeanWithSupplier));
//		return code;
//	}
//
//	private List<StackManipulation> createRegisterBeanCode(TypeDescription initializerType,
//			MethodDescription.InDefinedShape methodDescription) {
//		List<StackManipulation> code = new ArrayList<>();
//		code.addAll(getCodeToPrintln(":debug: Checking conditions before calling registerBean for bean factory method "
//				+ methodDescription.getName()));
//		// TODO would be better to create the registerBean code than wrap it
//		// recursively in condition checks
//		AnnotationList conditionalAnnotations = fetchConditionalAnnotations(methodDescription);
//		Label conditionsFailJumpTarget = new Label();
//		boolean fallbackRequired = false;
//		for (AnnotationDescription annoDescription : conditionalAnnotations) {
//			boolean handled = false;
//			for (ConditionalHandler handler : conditionalHandlers) {
//				if (handler.accept(annoDescription)) {
//					handled = true;
//					break;
//				}
//			}
//			if (!handled) {
//				log("Due to existence of " + annoDescription + " on " + methodDescription
//						+ " the fallback handler is being used for all conditions for this method");
//				fallbackRequired = true;
//			}
//		}
//		if (fallbackRequired) {
//			needsConditionService = true;
//			code.addAll(fallbackConditionHandler.computeStackManipulations(null, methodDescription,
//					conditionsFailJumpTarget));
//		} else {
//			for (AnnotationDescription annoDescription : conditionalAnnotations) {
//				for (ConditionalHandler handler : conditionalHandlers) {
//					if (handler.accept(annoDescription)) {
//						code.addAll(handler.computeStackManipulations(annoDescription, methodDescription,
//								conditionsFailJumpTarget));
//					}
//				}
//			}
//		}
//		code.addAll(getCodeToPrintln(
//				":debug: Calling registerBean for bean factory method " + methodDescription.getName()));
//		// Create code to call registerBean
//		code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
//		code.add(ClassConstant.of(methodDescription.getReturnType().asErasure()));
//		code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
//		MethodDescription.InDefinedShape lambda = new MethodDescription.Latent(initializerType,
//				"init_" + methodDescription.getName(), Modifier.PRIVATE | Modifier.STATIC, Collections.emptyList(),
//				methodDescription.getReturnType().asRawType(),
//				Collections.singletonList(new ParameterDescription.Token(
//						new TypeDescription.ForLoadedType(BeanFactory.class).asGenericType())),
//				Collections.emptyList(), Collections.emptyList(), null, null);
//		code.add(MethodInvocation.invoke(lambdaMeta).dynamic("get", new TypeDescription.ForLoadedType(Supplier.class),
//				Collections.singletonList(new TypeDescription.ForLoadedType(BeanFactory.class)),
//				Arrays.asList(JavaConstant.MethodType.of(get).asConstantPoolValue(),
//						JavaConstant.MethodHandle.of(lambda).asConstantPoolValue(),
//						JavaConstant.MethodType
//								.of(methodDescription.getReturnType().asErasure(), Collections.emptyList())
//								.asConstantPoolValue())));
//		code.add(ArrayFactory.forType(new TypeDescription.ForLoadedType(BeanDefinitionCustomizer.class).asGenericType())
//				.withValues(Collections.emptyList()));
//		code.add(MethodInvocation.invoke(registerBeanWithSupplier));
//		code.add(new InsertLabel(conditionsFailJumpTarget));
//		return code;
//	}
//
//	private AnnotationList fetchConditionalAnnotations(InDefinedShape methodDescription) {
//		AnnotationList list = methodDescription.getDeclaredAnnotations().filter(this::isConditionalAnnotation);
//		return list;
//	}
//
//	private List<AnnotationDescription> fetchConditionalAnnotations(TypeDescription typeDescription) {
//		List<AnnotationDescription> result = new ArrayList<>();
//		AnnotationList list = null;
//		try {
//			list = typeDescription.getDeclaredAnnotations().filter(this::isConditionalAnnotation);
//		} catch (Throwable t) {
//			throw new RuntimeException("Can't check annotations on " + typeDescription.getActualName(), t);
//		}
//		for (AnnotationDescription ad : list) {
//			result.add(ad);
//		}
//		while (typeDescription.isNestedClass()) {
//			typeDescription = typeDescription.getEnclosingType();
//			// System.out.println("Collecting conditions from outer class
//			// "+typeDescription);
//			list = typeDescription.getDeclaredAnnotations().filter(this::isConditionalAnnotation);
//			for (AnnotationDescription ad : list) {
//				result.add(ad);
//			}
//		}
//		return result;
//	}
//
//	public boolean isConditionalAnnotation(AnnotationDescription annoDescription) {
//		return isAnnotated(annoDescription, Conditional.class, new HashSet<>());
//	}
//
//	private boolean isAnnotated(AnnotationDescription desc, Class<? extends Annotation> annotationClass,
//			Set<AnnotationDescription> seen) {
//		seen.add(desc);
//		TypeDescription type = desc.getAnnotationType();
//		if (type.represents(annotationClass)) {
//			return true;
//		}
//		for (AnnotationDescription ann : type.getDeclaredAnnotations()) {
//			if (!seen.contains(ann) && isAnnotated(ann, annotationClass, seen)) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//}