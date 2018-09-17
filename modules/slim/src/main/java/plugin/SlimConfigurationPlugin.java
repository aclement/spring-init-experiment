package plugin;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.named;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationDescription.Loadable;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.annotation.AnnotationValue;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.ClassFileLocator.Resolution;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy.Default;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.implementation.bind.annotation.FieldProxy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.TypeCreation;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.collection.ArrayFactory;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.constant.NullConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.CompoundList;
import net.bytebuddy.utility.JavaConstant;
import slim.Module;
import slim.SlimConfiguration;

public class SlimConfigurationPlugin implements Plugin {

	private Generic Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext;

	private InitializerClassFactory initializerClassFactory;

	private ModuleClassFactory moduleClassFactory;

	public SlimConfigurationPlugin() {
		Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext = TypeDescription.Generic.Builder
				.parameterizedType(
						new TypeDescription.ForLoadedType(
								ApplicationContextInitializer.class),
						new TypeDescription.ForLoadedType(
								GenericApplicationContext.class))
				.build();
		initializerClassFactory = new InitializerClassFactory();
		moduleClassFactory = new ModuleClassFactory();
	}

	@Override
	public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassFileLocator locator) {
		try {
			File targetClassesFolder = locateTargetClasses(locator);
//			String path = "target/classes";
//			if (System.getProperty("project.basedir") != null) {
//				path = System.getProperty("project.basedir") + "/" + path;
//			}
			DynamicType initializerClassType = initializerClassFactory
					.make(typeDescription, locator);
			initializerClassType.saveIn(targetClassesFolder);

			// TODO: fix this so it creates a module properly (and only when needed - one
			// per app)
			if (hasAnnotation(typeDescription, SpringBootConfiguration.class)) {
				TypeDescription[] configs = findConfigs(typeDescription);
				DynamicType moduleClassType = moduleClassFactory.make(typeDescription,
						locator, configs);
				builder = addSlimConfigurationAnnotation(builder, moduleClassType);
				log("Saving: " + moduleClassType.getTypeDescription()+" in "+targetClassesFolder);
				moduleClassType.saveIn(targetClassesFolder);
			}

			builder = addInitializerMethod(builder, initializerClassType);

			return builder;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings("unchecked")
	private File locateTargetClasses(ClassFileLocator compoundLocator) {
		try {
			Field classFileLocatorsField = compoundLocator.getClass().getDeclaredField("classFileLocators");
			classFileLocatorsField.setAccessible(true);
			File found = null;
			List<ClassFileLocator> classFileLocators = (List<ClassFileLocator>) classFileLocatorsField.get(compoundLocator);
			for (ClassFileLocator classFileLocator: classFileLocators) {
				Field folderField = classFileLocator.getClass().getDeclaredField("folder");
				folderField.setAccessible(true);
				File ff = (File) folderField.get(classFileLocator);
				if (ff.toString().endsWith("target/classes")) {
					found = ff;
					break;
				}
			}
			return found;
		} catch (Exception e) {
			return null;
		}
	}

	private TypeDescription[] findConfigs(TypeDescription typeDescription) {
		Collection<TypeDescription> result = new LinkedHashSet<>();
		Loadable<Import> imports = typeDescription.getDeclaredAnnotations()
				.ofType(Import.class);
		log("Finding imports for " + typeDescription);
		MethodList<MethodDescription.InDefinedShape> methodList = TypeDescription.ForLoadedType
				.of(Import.class).getDeclaredMethods();
		InDefinedShape IMPORTS = methodList.filter(named("value")).getOnly();
		if (imports != null) {
			TypeDescription[] types = (TypeDescription[]) imports.getValue(IMPORTS)
					.resolve();
			for (TypeDescription type : types) {
				log("Import " + type);
				result.add(type);
			}
		}
		return result.toArray(new TypeDescription[0]);
	}

	private Builder<?> addSlimConfigurationAnnotation(DynamicType.Builder<?> builder,
			DynamicType initializerClassType) {
		return builder.annotateType(AnnotationDescription.Builder
				.ofType(SlimConfiguration.class)
				.defineTypeArray("module", initializerClassType.getTypeDescription())
				.build());
	}

	@Override
	public boolean matches(TypeDescription target) {
		return !hasAnnotation(target, SlimConfiguration.class)
				&& hasAnnotation(target, Configuration.class);
	}

	private boolean hasAnnotation(TypeDescription target,
			Class<? extends Annotation> annotation) {
		return target.getDeclaredAnnotations().stream()
				.anyMatch(desc -> isMetaAnnotated(desc, annotation));
	}

	private boolean isMetaAnnotated(AnnotationDescription desc,
			Class<? extends Annotation> annotation) {
		return isMetaAnnotated(desc, annotation, new HashSet<>());
	}

	private boolean isMetaAnnotated(AnnotationDescription desc,
			Class<? extends Annotation> annotation, Set<AnnotationDescription> seen) {
		seen.add(desc);
		TypeDescription type = desc.getAnnotationType();
		if (type.represents(annotation)) {
			return true;
		}
		for (AnnotationDescription ann : type.getDeclaredAnnotations()) {
			if (!seen.contains(ann) && isMetaAnnotated(ann, annotation, seen)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create $$initializer method in configuration class:
	 * 
	 * <pre>
	 * <code>
	 *  public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
	 *    return new SampleConfiguration.Initializer();
	 *  }
	 *  </code>
	 * 
	 * <pre>
	 */
	private DynamicType.Builder<?> addInitializerMethod(DynamicType.Builder<?> builder,
			DynamicType initializerClassType) {
		TypeDescription initializerClassTypeDescription = initializerClassType
				.getTypeDescription();
		InDefinedShape inDefinedShape = initializerClassTypeDescription
				.getDeclaredMethods().filter((em) -> em.isConstructor()).get(0);
		List<StackManipulation> code = new ArrayList<>();
		code.add(TypeCreation.of(initializerClassTypeDescription));
		code.add(Duplication.SINGLE);
		code.add(NullConstant.INSTANCE);
		code.add(MethodInvocation.invoke(inDefinedShape));
		code.add(MethodReturn.of(
				Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext));
		builder = builder.defineMethod("$$initializer",
				Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext,
				Visibility.PUBLIC, Ownership.STATIC)
				.intercept(new Implementation.Simple(new ByteCodeAppender.Simple(code)));
		return builder;
	}

	private void log(String message) {
		System.out.println(message);
	}

	/**
	 * For supporting the various @Conditional annotations.
	 */
	interface ConditionalHandler {
		boolean accept(AnnotationDescription description);

		Collection<? extends StackManipulation> computeStackManipulations(
				AnnotationDescription annoDescription, Object annotatedElement,
				Label conditionFailsLabel);
	}

	static abstract class BaseConditionalHandler implements ConditionalHandler {
		protected MethodDescription.InDefinedShape valueProperty;

		public BaseConditionalHandler(Class<?> annotationConditionClass) {
			try {
				valueProperty = new MethodDescription.ForLoadedMethod(
						annotationConditionClass.getMethod("value"));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	static class ConditionalOnMissingBeanHandler extends BaseConditionalHandler {

		public ConditionalOnMissingBeanHandler() {
			super(ConditionalOnMissingBean.class);
		}

		@Override
		public boolean accept(AnnotationDescription description) {
			return description.getAnnotationType()
					.represents(ConditionalOnMissingBean.class);
		}

		@Override
		public Collection<? extends StackManipulation> computeStackManipulations(
				AnnotationDescription annoDescription, Object annotatedElement,
				Label conditionFailsLabel) {
			try {
				List<StackManipulation> code = new ArrayList<>();
				AnnotationValue<?, ?> value = annoDescription.getValue(valueProperty);
				// TODO don't ignore that value since sometimes don't want to use the
				// return type of the annotated method
				// TypeDescription[] classes = (TypeDescription[]) value.resolve();

				// What to call: if (context.getBeanNamesForType(Gson.class).length == 0)
				// {
				code.add(MethodVariableAccess.REFERENCE.loadFrom(1)); // Load context
				TypeDescription returnTypeOfBeanMethod = ((MethodDescription.InDefinedShape) annotatedElement)
						.getReturnType().asErasure();
				code.add(ClassConstant.of(returnTypeOfBeanMethod));
				code.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(
						ConfigurableListableBeanFactory.class
								.getMethod("getBeanNamesForType", Class.class))));
				code.add(new ArrayLength());
				code.add(new IfNe(conditionFailsLabel));
				return code;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	static class ConditionalOnClassHandler extends BaseConditionalHandler {
		public ConditionalOnClassHandler() {
			super(ConditionalOnClass.class);
		}

		@Override
		public boolean accept(AnnotationDescription description) {
			return description.getAnnotationType().represents(ConditionalOnClass.class);
		}

		@Override
		public Collection<? extends StackManipulation> computeStackManipulations(
				AnnotationDescription annoDescription, Object annotatedElement,
				Label conditionFailsLabel) {
			try {
				List<StackManipulation> code = new ArrayList<>();
				AnnotationValue<?, ?> value = annoDescription.getValue(valueProperty);
				// TODO I would prefer the unresolved references...
				TypeDescription[] classes = (TypeDescription[]) value.resolve();
				for (int i = 0; i < classes.length; i++) {
					TypeDescription clazz = classes[i];
					code.add(new TextConstant(clazz.getName()));
					code.add(NullConstant.INSTANCE);
					// Call ClassUtils.isPresent("com.foo.Bar", null)
					code.add(
							MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(
									ClassUtils.class.getMethod("isPresent", String.class,
											ClassLoader.class))));
					code.add(new IfEq(conditionFailsLabel));
				}
				return code;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	static class IfEq implements StackManipulation {
		private final Label label;

		public IfEq(Label label) {
			this.label = label;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public Size apply(MethodVisitor methodVisitor, Context implementationContext) {
			methodVisitor.visitJumpInsn(Opcodes.IFEQ, label);
			return new StackManipulation.Size(-1, 0);
		}
	}

	static class IfNe implements StackManipulation {
		private final Label label;

		public IfNe(Label label) {
			this.label = label;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public Size apply(MethodVisitor methodVisitor, Context implementationContext) {
			methodVisitor.visitJumpInsn(Opcodes.IFNE, label);
			return new StackManipulation.Size(-1, 0);
		}
	}

	static class ArrayLength implements StackManipulation {
		public ArrayLength() {
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public Size apply(MethodVisitor methodVisitor, Context implementationContext) {
			methodVisitor.visitInsn(Opcodes.ARRAYLENGTH);
			return new StackManipulation.Size(0, 0);
		}
	}

	static class Goto implements StackManipulation {
		private final Label label;

		public Goto(Label label) {
			this.label = label;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public Size apply(MethodVisitor methodVisitor, Context implementationContext) {
			methodVisitor.visitJumpInsn(Opcodes.GOTO, label);
			return new StackManipulation.Size(0, 0);
		}
	}

	static class InsertLabel implements StackManipulation {
		private final Label label;

		public InsertLabel(Label label) {
			this.label = label;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public Size apply(MethodVisitor methodVisitor, Context implementationContext) {
			methodVisitor.visitLabel(label);
			return new StackManipulation.Size(0, 0);
		}
	}

	public class EnableFramesComputing implements AsmVisitorWrapper {
		@Override
		public final int mergeWriter(int flags) {
			return flags | ClassWriter.COMPUTE_FRAMES;
		}

		@Override
		public final int mergeReader(int flags) {
			return flags | ClassWriter.COMPUTE_FRAMES;
		}

		@Override
		public final ClassVisitor wrap(TypeDescription td, ClassVisitor cv,
				Implementation.Context ctx, TypePool tp,
				FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods,
				int wflags, int rflags) {
			return cv;
		}
	}

	// TODO change name to avoid clash ($$Initializer?)
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
	 *            if (ClassUtils.isPresent("Wobble",null)) { // @ConditionalOnClass(Wobble.class)
	 *                context.registerBean("foo", Foo.class,
	 *                        () -> context.getBean(SampleConfiguration.class).foo());
	 *            }
	 *            if (context.getBeanNamesForType(Bar.class).length == 0) { // @ConditionalOnMissingBean where method returns Bar
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

		private final MethodDescription.InDefinedShape registerBean,
				registerBeanWithSupplier, getBean, lambdaMeta, get;
		private List<ConditionalHandler> conditionalHandlers = new ArrayList<>();

		public InitializerClassFactory() {
			try {
				registerBean = new MethodDescription.ForLoadedMethod(
						GenericApplicationContext.class.getMethod("registerBean",
								Class.class, BeanDefinitionCustomizer[].class));
				registerBeanWithSupplier = new MethodDescription.ForLoadedMethod(
						GenericApplicationContext.class.getMethod("registerBean",
								Class.class, Supplier.class,
								BeanDefinitionCustomizer[].class));
				getBean = new MethodDescription.ForLoadedMethod(
						BeanFactory.class.getMethod("getBean", Class.class));
				lambdaMeta = new MethodDescription.ForLoadedMethod(LambdaMetafactory.class
						.getMethod("metafactory", MethodHandles.Lookup.class,
								String.class, MethodType.class, MethodType.class,
								MethodHandle.class, MethodType.class));
				get = new MethodDescription.ForLoadedMethod(
						Supplier.class.getMethod("get"));
			}
			catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
			conditionalHandlers.add(new ConditionalOnClassHandler());
			conditionalHandlers.add(new ConditionalOnMissingBeanHandler());
		}

		public DynamicType make(TypeDescription configurationTypeDescription,
				ClassFileLocator locator) throws Exception {
			DynamicType.Builder<?> builder = new ByteBuddy().subclass(
					Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext,
					Default.NO_CONSTRUCTORS).visit(new EnableFramesComputing()) // Might
																				// need
																				// this as
																				// conditional
																				// checks
																				// alter
																				// stacks/frames
			;

			// TODO how to do logging from a bytebuddy plugin?
			log("Generating initializer for " + configurationTypeDescription.getName());

			builder = builder.modifiers(Modifier.STATIC)
					.name(configurationTypeDescription.getTypeName() + "$Initializer");

			TypeDescription target = builder.make().getTypeDescription();

			// TODO is there a ByteBuddy strategy for doing what javac does for private
			// inner classes?

			// Copy javac: create package private constructor visible from @Configuration
			// type
			// TODO why extra unnecessary bytecode in the generated ctor? (see extraneous
			// DUP/POP)
			builder = builder.defineConstructor(Visibility.PACKAGE_PRIVATE)
					.withParameter(target)
					.intercept(MethodCall.invoke(Object.class.getDeclaredConstructor()));
			// Make the default ctor private
			builder = builder.defineConstructor(Visibility.PRIVATE)
					.intercept(MethodCall.invoke(Object.class.getDeclaredConstructor()));

			List<StackManipulation> code = new ArrayList<>();

			// Process the conditions on the top level configuration type
			AnnotationList conditionalAnnotations = fetchConditionalAnnotations(
					configurationTypeDescription);
			Label typeConditionsFailJumpTarget = new Label();
			for (AnnotationDescription annoDescription : conditionalAnnotations) {
				for (ConditionalHandler handler : conditionalHandlers) {
					if (handler.accept(annoDescription)) {
						code.addAll(handler.computeStackManipulations(annoDescription,
								configurationTypeDescription,
								typeConditionsFailJumpTarget));
					}
				}
			}

			// Store a reusable empty array of BeanDefinitionCustomizer
			code.add(ArrayFactory
					.forType(new TypeDescription.ForLoadedType(
							BeanDefinitionCustomizer.class).asGenericType())
					.withValues(Collections.emptyList()));
			code.add(MethodVariableAccess.REFERENCE.storeAt(2));

			// Call context.registerBean(SampleConfiguration.class)
			code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
			code.add(ClassConstant.of(configurationTypeDescription));
			code.add(MethodVariableAccess.REFERENCE.loadFrom(2));
			code.add(MethodInvocation.invoke(registerBean));

			for (MethodDescription.InDefinedShape methodDescription : configurationTypeDescription
					.getDeclaredMethods().filter(isAnnotatedWith(Bean.class))) {

				List<StackManipulation> stackManipulations = new ArrayList<>();
				for (TypeDescription argumentType : methodDescription.isStatic()
						? methodDescription.getParameters().asTypeList().asErasures()
						: CompoundList.of(configurationTypeDescription, methodDescription
								.getParameters().asTypeList().asErasures())) {
					stackManipulations.add(MethodVariableAccess.REFERENCE.loadFrom(0));
					stackManipulations.add(ClassConstant.of(argumentType));
					stackManipulations.add(MethodInvocation.invoke(getBean));
					stackManipulations.add(TypeCasting.to(argumentType));
				}
				stackManipulations.add(MethodInvocation.invoke(methodDescription));
				stackManipulations
						.add(MethodReturn.of(methodDescription.getReturnType()));

				builder = builder
						.defineMethod("init_" + methodDescription.getName(),
								methodDescription.getReturnType().asErasure(),
								Visibility.PRIVATE, Ownership.STATIC)
						.withParameters(BeanFactory.class)
						.intercept(new Implementation.Simple(
								new ByteCodeAppender.Simple(stackManipulations)));

				code.addAll(createRegisterBeanCode(target, methodDescription));
			}

			code.add(new InsertLabel(typeConditionsFailJumpTarget));
			code.add(MethodReturn.VOID);

			// Create the initialize() method
			builder = builder
					.method(named("initialize")
							.and(isDeclaredBy(ApplicationContextInitializer.class)))
					.intercept(
							new Implementation.Simple(new ByteCodeAppender.Simple(code)));

			return builder.make();
		}

		private List<StackManipulation> createRegisterBeanCode(
				TypeDescription initializerType,
				MethodDescription.InDefinedShape methodDescription) {
			List<StackManipulation> code = new ArrayList<>();

			// TODO would be better to create the registerBean code than wrap it
			// recursively in condition checks
			AnnotationList conditionalAnnotations = fetchConditionalAnnotations(
					methodDescription);
			Label conditionsFailJumpTarget = new Label();
			for (AnnotationDescription annoDescription : conditionalAnnotations) {
				for (ConditionalHandler handler : conditionalHandlers) {
					if (handler.accept(annoDescription)) {
						code.addAll(handler.computeStackManipulations(annoDescription,
								methodDescription, conditionsFailJumpTarget));
					}
				}
			}

			// Create code to call registerBean
			code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
			code.add(ClassConstant.of(methodDescription.getReturnType().asErasure()));
			code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
			MethodDescription.InDefinedShape lambda = new MethodDescription.Latent(
					initializerType, "init_" + methodDescription.getName(),
					Modifier.PRIVATE | Modifier.STATIC, Collections.emptyList(),
					methodDescription.getReturnType().asRawType(),
					Collections.singletonList(new ParameterDescription.Token(
							new TypeDescription.ForLoadedType(BeanFactory.class)
									.asGenericType())),
					Collections.emptyList(), Collections.emptyList(), null, null);
			code.add(MethodInvocation.invoke(lambdaMeta).dynamic("get",
					new TypeDescription.ForLoadedType(Supplier.class),
					Collections.singletonList(
							new TypeDescription.ForLoadedType(BeanFactory.class)),
					Arrays.asList(JavaConstant.MethodType.of(get).asConstantPoolValue(),
							JavaConstant.MethodHandle.of(lambda).asConstantPoolValue(),
							JavaConstant.MethodType
									.of(methodDescription.getReturnType().asErasure(),
											Collections.emptyList())
									.asConstantPoolValue())));
			code.add(ArrayFactory
					.forType(new TypeDescription.ForLoadedType(
							BeanDefinitionCustomizer.class).asGenericType())
					.withValues(Collections.emptyList()));
			code.add(MethodInvocation.invoke(registerBeanWithSupplier));
			code.add(new InsertLabel(conditionsFailJumpTarget));
			return code;
		}

		private AnnotationList fetchConditionalAnnotations(
				InDefinedShape methodDescription) {
			AnnotationList list = methodDescription.getDeclaredAnnotations()
					.filter(this::isConditionalAnnotation);
			return list;
		}

		private AnnotationList fetchConditionalAnnotations(
				TypeDescription typeDescription) {
			AnnotationList list = typeDescription.getDeclaredAnnotations()
					.filter(this::isConditionalAnnotation);
			return list;
		}

		public boolean isConditionalAnnotation(AnnotationDescription annoDescription) {
			return isAnnotated(annoDescription, Conditional.class, new HashSet<>());
		}

		private boolean isAnnotated(AnnotationDescription desc,
				Class<? extends Annotation> annotationClass,
				Set<AnnotationDescription> seen) {
			seen.add(desc);
			TypeDescription type = desc.getAnnotationType();
			if (type.represents(annotationClass)) {
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

	class ModuleClassFactory {

		public ModuleClassFactory() {
		}

		private String toModuleName(String typename) {
			if (typename.endsWith("Configuration")) {
				return typename.substring(0, typename.indexOf("Configuration"))
						+ "Module";
			}
			else if (typename.endsWith("Application")) {
				return typename.substring(0, typename.indexOf("Application")) + "Module";
			}
			return typename + "Module";
		}

		public DynamicType make(TypeDescription typeDescription, ClassFileLocator locator,
				TypeDescription... typesWithInitializeMethods) throws Exception {
			log("Generating module for " + typeDescription.getName() + " calling");
			for (TypeDescription td : typesWithInitializeMethods) {
				log("- " + td);
			}
			String moduleName = toModuleName(typeDescription.getTypeName());
			DynamicType.Builder<?> builder = new ByteBuddy().subclass(Module.class)
					.name(moduleName);
			log("Module: " + moduleName);

			Generic Type_ACI = TypeDescription.Generic.Builder
					.rawType(ApplicationContextInitializer.class).build();

			List<StackManipulation> code = new ArrayList<>();
			List<StackManipulation> eachElement = new ArrayList<>();

			Generic Type_ListOfACI = TypeDescription.Generic.Builder.parameterizedType(
					new TypeDescription.ForLoadedType(List.class),
					Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext)
					.build();

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
			code.add(ArrayFactory
					.forType(new TypeDescription.ForLoadedType(
							ApplicationContextInitializer.class).asGenericType())
					.withValues(eachElement));
			
			code.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(
					Arrays.class.getMethod("asList", Object[].class))));
			code.add(MethodReturn.of(
					Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext));

			builder = builder
					.defineMethod("initializers", Type_ListOfACI, Modifier.PUBLIC)
					.intercept(
							new Implementation.Simple(new ByteCodeAppender.Simple(code)));

			return builder.make();
		}

	}

}
