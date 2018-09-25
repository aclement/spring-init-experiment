package plugin;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.env.PropertyResolver;
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
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ImplementationDefinition;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy.Default;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.Implementation.Context;
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
import slim.ConditionService;
import slim.InitializerMapping;
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
			if (targetClassesFolder == null) {
				log("Unable to determine target/classes folder for module");
				targetClassesFolder = new File("target/classes");
			}
			// TODO: skip this class if it was already processed
			DynamicType initializerClassType = initializerClassFactory
					.make(typeDescription, locator);
			initializerClassType.saveIn(targetClassesFolder);
			// TODO: fix this so it creates a module properly (and only when needed - one
			// per app)
			if (hasAnnotation(typeDescription, SpringBootConfiguration.class)) {
				TypeDescription[] configs = findConfigs(typeDescription);
				log("Discovering @Import on " + typeDescription.getActualName()
						+ ", found: " + toString(configs));
				// CRUDE - Create a subset of the configs where those that have module
				// references are not included (i.e. presume
				// the module reference will do the initialization stuff instead of
				// looking for a $$initializer in the config)
				List<TypeDescription> configSubset = new ArrayList<>();
				configSubset.add(typeDescription);
				for (TypeDescription config : configs) {
					// Name based - crude...
					boolean skip = false;
					System.out.println(config.getSimpleName());
					// Exclude spring library imports (they won't have the $$initializer
					// method)
					if (!hasAnnotation(config, SlimConfiguration.class)
							&& config.getName().startsWith("org.springframework")) {
						skip = true;
					}
					if (!skip) {
						System.out.println("Not skipping " + config);
						configSubset.add(config);
					}
					else {
						System.out.println("Skipping " + config);
					}
				}
				DynamicType moduleClassType = moduleClassFactory.make(typeDescription,
						locator, configSubset.toArray(new TypeDescription[0]));
				builder = addSlimConfigurationAnnotation(
						builder, moduleClassType);
				log("Saving: " + moduleClassType.getTypeDescription() + " in "
						+ targetClassesFolder);
				moduleClassType.saveIn(targetClassesFolder);
			}
			builder = addInitializerMethod(builder, initializerClassType);

			return builder;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private String toString(TypeDescription[] tds) {
		StringBuilder s = new StringBuilder();
		s.append("[");
		if (tds != null) {
			for (int i = 0; i < tds.length; i++) {
				if (i > 0)
					s.append(",");
				s.append(tds[i].getName());
			}
		}
		s.append("]");
		return s.toString();
	}

	@SuppressWarnings("unchecked")
	private File locateTargetClasses(ClassFileLocator compoundLocator) {
		try {
			Field classFileLocatorsField = compoundLocator.getClass()
					.getDeclaredField("classFileLocators");
			classFileLocatorsField.setAccessible(true);
			File found = null;
			List<ClassFileLocator> classFileLocators = (List<ClassFileLocator>) classFileLocatorsField
					.get(compoundLocator);
			for (ClassFileLocator classFileLocator : classFileLocators) {
				Field folderField = classFileLocator.getClass()
						.getDeclaredField("folder");
				folderField.setAccessible(true);
				File ff = (File) folderField.get(classFileLocator);
				if (ff.toString().endsWith("target/classes")) {
					found = ff;
					break;
				}
			}
			return found;
		}
		catch (Exception e) {
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
				if (!type.isAssignableTo(Module.class)) {
					log("Import " + type);
					result.add(type);
				}
			}
		}
		return result.toArray(new TypeDescription[0]);
	}

	private Builder<?> addSlimConfigurationAnnotation(
			DynamicType.Builder<?> builder, DynamicType initializerClassType) {
		List<TypeDescription> initializers = new ArrayList<>();
		initializers.add(initializerClassType.getTypeDescription());
		return builder.annotateType(AnnotationDescription.Builder
				.ofType(SlimConfiguration.class)
				.defineTypeArray("module", initializers.toArray(new TypeDescription[0]))
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
		ImplementationDefinition<?> method = builder.defineMethod("$$initializer",
				Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext,
				Visibility.PUBLIC, Ownership.STATIC);
		builder = method
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
			if (annotationConditionClass != null) {
				try {
					valueProperty = new MethodDescription.ForLoadedMethod(
							annotationConditionClass.getMethod("value"));
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	static class FallbackConditionHandler implements ConditionalHandler {

		@Override
		public boolean accept(AnnotationDescription description) {
			return true;
		}

		@Override
		public Collection<? extends StackManipulation> computeStackManipulations(
				AnnotationDescription annoDescription, Object annotatedElement,
				Label conditionFailsLabel) {
			try {
				List<StackManipulation> code = new ArrayList<>();
				if (annotatedElement instanceof MethodDescription) {
					// Call ConditionService.matches(ConfigurationClass, BeanClass)
					code.add(MethodVariableAccess.REFERENCE.loadFrom(3));
					code.add(ClassConstant.of(((MethodDescription) annotatedElement)
							.getDeclaringType().asErasure()));
					code.add(ClassConstant.of(((MethodDescription) annotatedElement)
							.getReturnType().asErasure()));
					code.add(MethodInvocation.invoke(
							new MethodDescription.ForLoadedMethod(ConditionService.class
									.getMethod("matches", Class.class, Class.class))));
					code.add(new IfEq(conditionFailsLabel));
				}
				else {
					// Call ConditionService.matches(Class)
					code.add(MethodVariableAccess.REFERENCE.loadFrom(3));
					code.add(ClassConstant.of((TypeDescription) annotatedElement));
					code.add(MethodInvocation.invoke(
							new MethodDescription.ForLoadedMethod(ConditionService.class
									.getMethod("matches", Class.class))));
					code.add(new IfEq(conditionFailsLabel));
				}
				return code;
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

	static class ProfileConditionHandler extends BaseConditionalHandler {
		public ProfileConditionHandler() {
			super(Profile.class);
		}

		@Override
		public boolean accept(AnnotationDescription description) {
			return description.getAnnotationType().represents(Profile.class);
		}

		@Override
		public Collection<? extends StackManipulation> computeStackManipulations(
				AnnotationDescription annoDescription, Object annotatedElement,
				Label conditionFailsLabel) {
			try {
				// Invoke: context.getEnvironment().acceptsProfiles(Profiles.of((String[])
				// value))
				List<StackManipulation> code = new ArrayList<>();
				AnnotationValue<?, ?> value = annoDescription.getValue(valueProperty);
				// TODO I would prefer the unresolved references...
				String[] profiles = (String[]) value.resolve();
				List<StackManipulation> profilesArrayEntries = new ArrayList<>();
				for (int i = 0; i < profiles.length; i++) {
					profilesArrayEntries.add(new TextConstant(profiles[i]));
				}
				// ALOAD 1
				// INVOKEVIRTUAL
				// org/springframework/context/support/GenericApplicationContext.getEnvironment()Lorg/springframework/core/env/ConfigurableEnvironment;
				// ALOAD 5
				// CHECKCAST [Ljava/lang/String;
				// CHECKCAST [Ljava/lang/String;
				// INVOKESTATIC
				// org/springframework/core/env/Profiles.of([Ljava/lang/String;)Lorg/springframework/core/env/Profiles;
				// INVOKEINTERFACE
				// org/springframework/core/env/Environment.acceptsProfiles(Lorg/springframework/core/env/Profiles;)Z
				// IFEQ L7

				code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
				code.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(
						GenericApplicationContext.class.getMethod("getEnvironment"))));
				code.add(
						ArrayFactory
								.forType(new TypeDescription.ForLoadedType(String.class)
										.asGenericType())
								.withValues(profilesArrayEntries));
				code.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(
						Profiles.class.getDeclaredMethod("of", String[].class))));
				code.add(MethodInvocation
						.invoke(new MethodDescription.ForLoadedMethod(Environment.class
								.getDeclaredMethod("acceptsProfiles", Profiles.class))));
				code.add(new IfEq(conditionFailsLabel));
				return code;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	static class ConditionalOnPropertyHandler extends BaseConditionalHandler {
		protected MethodDescription.InDefinedShape prefixProperty;
		protected MethodDescription.InDefinedShape nameProperty;
		protected MethodDescription.InDefinedShape matchIfMissingProperty;
		protected MethodDescription.InDefinedShape havingValueProperty;

		public ConditionalOnPropertyHandler() {
			super(ConditionalOnProperty.class);
			try {
				prefixProperty = new MethodDescription.ForLoadedMethod(
						ConditionalOnProperty.class.getMethod("prefix"));
				nameProperty = new MethodDescription.ForLoadedMethod(
						ConditionalOnProperty.class.getMethod("name"));
				matchIfMissingProperty = new MethodDescription.ForLoadedMethod(
						ConditionalOnProperty.class.getMethod("matchIfMissing"));
				havingValueProperty = new MethodDescription.ForLoadedMethod(
						ConditionalOnProperty.class.getMethod("havingValue"));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean accept(AnnotationDescription description) {
			if (!description.getAnnotationType()
					.represents(ConditionalOnProperty.class)) {
				return false;
			}
			AnnotationValue<?, ?> av = description.getValue(havingValueProperty);
			String havingValue = (String) av.resolve();
			if (havingValue.length() != 0) {
				System.out.println("Unable to optimize " + description
						+ " because havingValue is set");
				return false;
			}
			return true;
		}

		/**
		 * What this does - looks at ConditionalOnProperty annotation for a value or name
		 * being set as one or more property names. It checks the prefix to see if that
		 * has been set. Using this information it creates a list of properties to check
		 * for. It cannot handle havingValue being set right now. It does understand
		 * matchIfMissing.
		 */
		@Override
		public Collection<? extends StackManipulation> computeStackManipulations(
				AnnotationDescription annoDescription, Object annotatedElement,
				Label conditionFailsLabel) {
			try {

				// Iterate over properties calling
				// PropertyResolver.containsProperty(Ljava/lang/String;)Z
				List<StackManipulation> code = new ArrayList<>();

				AnnotationValue<?, ?> mim = annoDescription
						.getValue(matchIfMissingProperty);
				boolean matchIfMissing = (Boolean) mim.resolve();
				AnnotationValue<?, ?> prefix = annoDescription.getValue(prefixProperty);
				String prefixString = (String) prefix.resolve();
				AnnotationValue<?, ?> value = annoDescription.getValue(valueProperty);
				Object resolvedValue = value.resolve();
				if (resolvedValue == null) {
					resolvedValue = annoDescription.getValue(nameProperty).resolve();
				}
				List<String> properties = new ArrayList<>();
				if (resolvedValue instanceof String) {
					properties.add((String) resolvedValue);
				}
				else {
					for (String propertyString : (String[]) resolvedValue) {
						String propertyToCheck = (prefixString != null
								&& prefixString.length() != 0)
										? prefix + "." + propertyString
										: propertyString;
						properties.add(propertyToCheck);
					}
				}
				resolvedValue = annoDescription.getValue(nameProperty).resolve();
				if (resolvedValue instanceof String) {
					properties.add((String) resolvedValue);
				}
				else {
					for (String propertyString : (String[]) resolvedValue) {
						String propertyToCheck = (prefixString != null
								&& prefixString.length() != 0)
										? prefix + "." + propertyString
										: propertyString;
						properties.add(propertyToCheck);
					}
				}

				if (properties.size() != 0) {
					System.out.println("Processing ConditionalOnProperty found on "
							+ annotatedElement);
				}

				code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
				code.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(
						GenericApplicationContext.class.getMethod("getEnvironment"))));
				code.add(MethodVariableAccess.REFERENCE.storeAt(4));

				for (String propertyString : properties) {
					System.out.println("inserting " + (matchIfMissing ? "negative " : "")
							+ "check for property '" + propertyString + "'");
					code.add(MethodVariableAccess.REFERENCE.loadFrom(4));
					code.add(new TextConstant(propertyString));
					code.add(MethodInvocation.invoke(
							new MethodDescription.ForLoadedMethod(PropertyResolver.class
									.getMethod("containsProperty", String.class))));
					if (matchIfMissing) {
						code.add(new IfNe(conditionFailsLabel));
					}
					else {
						code.add(new IfEq(conditionFailsLabel));
					}
				}
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
				registerBeanWithSupplier, getBean, lambdaMeta, get, getBeanFactory;
		private List<ConditionalHandler> conditionalHandlers = new ArrayList<>();
		private boolean needsConditionService;
		private FallbackConditionHandler fallbackConditionHandler;

		public InitializerClassFactory() {
			try {
				getBeanFactory = new MethodDescription.ForLoadedMethod(
						GenericApplicationContext.class.getMethod("getBeanFactory"));
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
			conditionalHandlers.add(new ProfileConditionHandler());
			conditionalHandlers.add(new ConditionalOnPropertyHandler());
			needsConditionService = false;
			fallbackConditionHandler = new FallbackConditionHandler();
		}

		public DynamicType make(TypeDescription configurationTypeDescription,
				ClassFileLocator locator) throws Exception {
			DynamicType.Builder<?> builder = new ByteBuddy().subclass(
					Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext,
					Default.NO_CONSTRUCTORS).visit(new EnableFramesComputing());

			// TODO how to do logging from a bytebuddy plugin?
			log("Generating initializer for " + configurationTypeDescription.getName());

			builder = builder.modifiers(Modifier.STATIC)
					.name(configurationTypeDescription.getTypeName() + "$Initializer");
			builder.annotateType(AnnotationDescription.Builder
					.ofType(InitializerMapping.class)
					.defineTypeArray("value",
							new TypeDescription[] { configurationTypeDescription })
					.build());

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

			Label typeConditionsFailJumpTarget = new Label();
			processConfigurationTypeLevelConditions(configurationTypeDescription,
					fallbackConditionHandler, code, typeConditionsFailJumpTarget);

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

			if (needsConditionService) {
				TypeDescription conditionServiceTypeDescription = new TypeDescription.ForLoadedType(
						ConditionService.class);
				code.add(0, MethodVariableAccess.REFERENCE.loadFrom(1));
				code.add(1, MethodInvocation.invoke(getBeanFactory));
				code.add(2, ClassConstant.of(conditionServiceTypeDescription));
				code.add(3, MethodInvocation.invoke(getBean));
				code.add(4, TypeCasting.to(conditionServiceTypeDescription));
				code.add(5, MethodVariableAccess.REFERENCE.storeAt(3));
			}

			// Create the initialize() method
			builder = builder
					.method(named("initialize")
							.and(isDeclaredBy(ApplicationContextInitializer.class)))
					.intercept(
							new Implementation.Simple(new ByteCodeAppender.Simple(code)));

			return builder.make();
		}

		private void processConfigurationTypeLevelConditions(
				TypeDescription configurationTypeDescription,
				FallbackConditionHandler fallbackConditionHandler,
				List<StackManipulation> code, Label typeConditionsFailJumpTarget) {
			// Process the conditions on the this type and any outer types
			List<AnnotationDescription> conditionalAnnotations = fetchConditionalAnnotations(
					configurationTypeDescription);
			if (conditionalAnnotations.size() == 0) {
				log("No type level conditions on " + configurationTypeDescription);
			}
			else {
				log("Applying the following type level conditions to "
						+ configurationTypeDescription + ": " + conditionalAnnotations);
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
					log("Due to existence of " + annoDescription + " on "
							+ configurationTypeDescription
							+ " the fallback handler is being used for all conditions on the type");
					fallbackRequired = true;
					needsConditionService = true;
				}
			}
			if (fallbackRequired) {
				code.addAll(fallbackConditionHandler.computeStackManipulations(null,
						configurationTypeDescription, typeConditionsFailJumpTarget));
			}
			else {
				for (AnnotationDescription annoDescription : conditionalAnnotations) {
					for (ConditionalHandler handler : conditionalHandlers) {
						if (handler.accept(annoDescription)) {
							code.addAll(handler.computeStackManipulations(annoDescription,
									configurationTypeDescription,
									typeConditionsFailJumpTarget));
						}
					}
				}
			}
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
					log("Due to existence of " + annoDescription + " on "
							+ methodDescription
							+ " the fallback handler is being used for all conditions for this method");
					fallbackRequired = true;
				}
			}
			if (fallbackRequired) {
				needsConditionService = true;
				code.addAll(fallbackConditionHandler.computeStackManipulations(null,
						methodDescription, conditionsFailJumpTarget));
			}
			else {
				for (AnnotationDescription annoDescription : conditionalAnnotations) {
					for (ConditionalHandler handler : conditionalHandlers) {
						if (handler.accept(annoDescription)) {
							code.addAll(handler.computeStackManipulations(annoDescription,
									methodDescription, conditionsFailJumpTarget));
						}
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

		private List<AnnotationDescription> fetchConditionalAnnotations(
				TypeDescription typeDescription) {
			List<AnnotationDescription> result = new ArrayList<>();
			AnnotationList list = typeDescription.getDeclaredAnnotations()
					.filter(this::isConditionalAnnotation);
			for (AnnotationDescription ad : list) {
				result.add(ad);
			}
			while (typeDescription.isNestedClass()) {
				typeDescription = typeDescription.getEnclosingType();
				// System.out.println("Collecting conditions from outer class
				// "+typeDescription);
				list = typeDescription.getDeclaredAnnotations()
						.filter(this::isConditionalAnnotation);
				for (AnnotationDescription ad : list) {
					result.add(ad);
				}
			}
			return result;
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
