package plugin;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.io.File;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
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
import net.bytebuddy.implementation.bytecode.constant.NullConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.utility.CompoundList;
import net.bytebuddy.utility.JavaConstant;

public class SlimConfigurationPlugin implements Plugin {
	
	private Generic Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext;
	
	private InitializerClassFactory initializerClassFactory;

//	private ModuleClassFactory moduleClassFactory;
	
	public SlimConfigurationPlugin() {
		Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext =
				TypeDescription.Generic.Builder.parameterizedType(
						new TypeDescription.ForLoadedType(ApplicationContextInitializer.class),
						new TypeDescription.ForLoadedType(GenericApplicationContext.class)).build();
		initializerClassFactory = new InitializerClassFactory();
//		moduleClassFactory = new ModuleClassFactory();
	}

	@Override
	public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassFileLocator locator) {
		try {
			String path = "target/classes";
			if (System.getProperty("project.basedir") != null) {
				path = System.getProperty("project.basedir") + "/" + path;
			}
			DynamicType initializerClassType = initializerClassFactory.make(typeDescription, locator);
			initializerClassType.saveIn(new File(path));
			
//			DynamicType moduleClassType = moduleClassFactory.make(typeDescription, locator, typeDescription, typeDescription);
//			moduleClassType.saveIn(new File(path));
			
			builder = addSlimConfigurationAnnotation(builder, initializerClassType);
			builder = addInitializerMethod(builder, initializerClassType);
			
			return builder;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	
	}

	private Builder<?> addSlimConfigurationAnnotation(DynamicType.Builder<?> builder,
			DynamicType initializerClassType) {
		return builder
				.annotateType(AnnotationDescription.Builder.ofType(SlimConfiguration.class)
				.defineTypeArray("type", initializerClassType.getTypeDescription()).build());
	}

    @Override
    public boolean matches(TypeDescription target) {
        return !target.getDeclaredAnnotations().isAnnotationPresent(SlimConfiguration.class)
                && target.getDeclaredAnnotations().stream().anyMatch(this::isConfiguration);
    }

    private boolean isConfiguration(AnnotationDescription desc) {
        return isConfiguration(desc, new HashSet<>());
    }

    private boolean isConfiguration(AnnotationDescription desc, Set<AnnotationDescription> seen) {
        seen.add(desc);
        TypeDescription type = desc.getAnnotationType();
        if (type.represents(Configuration.class)) {
            return true;
        }
        for (AnnotationDescription ann : type.getDeclaredAnnotations()) {
            if (!seen.contains(ann) && isConfiguration(ann, seen)) {
                return true;
            }
        }
        return false;
    }

    /**
     *  Create $$initializer method in configuration class:
     *  <pre><code>
     *  public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
	 *    return new SampleConfiguration.Initializer();
	 *  }
	 *  </code><pre>
	 */
	private DynamicType.Builder<?> addInitializerMethod(DynamicType.Builder<?> builder, DynamicType initializerClassType) {
		TypeDescription initializerClassTypeDescription = initializerClassType.getTypeDescription();
		InDefinedShape inDefinedShape = initializerClassTypeDescription.getDeclaredMethods().filter((em) -> em.isConstructor()).get(0);
		List<StackManipulation> code = new ArrayList<>();
		code.add(TypeCreation.of(initializerClassTypeDescription));
		code.add(Duplication.SINGLE);
		code.add(NullConstant.INSTANCE);
		code.add(MethodInvocation.invoke(inDefinedShape));
		code.add(MethodReturn.of(Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext));
		builder = builder.defineMethod("$$initializer",
				Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext, Visibility.PUBLIC,
				Ownership.STATIC).intercept(new Implementation.Simple(new ByteCodeAppender.Simple(code)));
		return builder;
	}
	
	private void log(String message) {
		System.out.println(message);
	}

	// TODO change name to avoid clash ($$Initializer?)
	/**
	 * Constructs the Initializer inner class. Something like this:
	 * <pre><code>
	 *  private static class Initializer implements ApplicationContextInitializer<GenericApplicationContext> {
	 *
	 *        @Override
	 *        public void initialize(GenericApplicationContext context) {
	 *            context.registerBean(SampleConfiguration.class);
	 *            // All @Bean methods get registered like this:
	 *            context.registerBean("foo", Foo.class,
	 *                    () -> context.getBean(SampleConfiguration.class).foo());
	 *            context.registerBean("bar", Bar.class, () -> context
	 *                    .getBean(SampleConfiguration.class).bar(context.getBean(Foo.class)));
	 *            context.registerBean("runner", CommandLineRunner.class,
	 *                    () -> context.getBean(SampleConfiguration.class)
	 *                            .runner(context.getBean(Bar.class)));
	 *        }
	 * }
	 * </code></pre>
	 */
	class InitializerClassFactory {

		private final MethodDescription.InDefinedShape registerBean, registerBeanWithSupplier, getBean, lambdaMeta, get;

		public InitializerClassFactory() {
			try {
				registerBean = new MethodDescription.ForLoadedMethod(GenericApplicationContext.class
						.getMethod("registerBean", Class.class, BeanDefinitionCustomizer[].class));
				registerBeanWithSupplier = new MethodDescription.ForLoadedMethod(GenericApplicationContext.class
						.getMethod("registerBean", Class.class, Supplier.class, BeanDefinitionCustomizer[].class));
				getBean = new MethodDescription.ForLoadedMethod(BeanFactory.class.getMethod("getBean", Class.class));
				lambdaMeta = new MethodDescription.ForLoadedMethod(
						LambdaMetafactory.class.getMethod("metafactory", MethodHandles.Lookup.class, String.class,
								MethodType.class, MethodType.class, MethodHandle.class, MethodType.class));
				get = new MethodDescription.ForLoadedMethod(Supplier.class.getMethod("get"));
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}

		public DynamicType make(TypeDescription typeDescription, ClassFileLocator locator) throws Exception {
			DynamicType.Builder<?> builder = new ByteBuddy().subclass(
					Type_ParameterizedApplicationContextInitializerWithGenericApplicationContext,
					Default.NO_CONSTRUCTORS);

			// TODO how to do logging from a bytebuddy plugin?
			log("Generating initializer for "+typeDescription.getName());
			
			builder = builder.modifiers(Modifier.STATIC).name(typeDescription.getTypeName() + "$Initializer");

			TypeDescription target = builder.make().getTypeDescription();

			// TODO is there a ByteBuddy strategy for doing what javac does for private inner classes?
			
			// Copy javac: create package private constructor visible from @Configuration type
			// TODO why extra unnecessary bytecode in the generated ctor? (see extraneous DUP/POP)
			builder = builder
					.defineConstructor(Visibility.PACKAGE_PRIVATE)
					.withParameter(target)
					.intercept(MethodCall.invoke(Object.class.getDeclaredConstructor()));
			// Make the default ctor private
			builder = builder
					.defineConstructor(Visibility.PRIVATE)
					.intercept(MethodCall.invoke(Object.class.getDeclaredConstructor()));

			
			List<StackManipulation> initializers = new ArrayList<>();

			// Store a reusable empty array of BeanDefinitionCustomizer
			initializers.add(ArrayFactory
					.forType(new TypeDescription.ForLoadedType(BeanDefinitionCustomizer.class).asGenericType())
					.withValues(Collections.emptyList()));
			initializers.add(MethodVariableAccess.REFERENCE.storeAt(2));
			
			// Call context.registerBean(SampleConfiguration.class)
			initializers.add(MethodVariableAccess.REFERENCE.loadFrom(1));
			initializers.add(ClassConstant.of(typeDescription));
			initializers.add(MethodVariableAccess.REFERENCE.loadFrom(2));
			initializers.add(MethodInvocation.invoke(registerBean));

			
			for (MethodDescription.InDefinedShape methodDescription : typeDescription.getDeclaredMethods()
					.filter(isAnnotatedWith(Bean.class))) {
				List<StackManipulation> stackManipulations = new ArrayList<>();
				for (TypeDescription argumentType : methodDescription.isStatic()
						? methodDescription.getParameters().asTypeList().asErasures()
						: CompoundList.of(typeDescription,
								methodDescription.getParameters().asTypeList().asErasures())) {
					stackManipulations.add(MethodVariableAccess.REFERENCE.loadFrom(0));
					stackManipulations.add(ClassConstant.of(argumentType));
					stackManipulations.add(MethodInvocation.invoke(getBean));
					stackManipulations.add(TypeCasting.to(argumentType));
				}
				stackManipulations.add(MethodInvocation.invoke(methodDescription));
				stackManipulations.add(MethodReturn.of(methodDescription.getReturnType()));

				builder = builder
						.defineMethod("init_" + methodDescription.getName(),
								methodDescription.getReturnType().asErasure(), Visibility.PRIVATE, Ownership.STATIC)
						.withParameters(BeanFactory.class)
						.intercept(new Implementation.Simple(new ByteCodeAppender.Simple(stackManipulations)));

				initializers.add(MethodVariableAccess.REFERENCE.loadFrom(1));
				initializers.add(ClassConstant.of(methodDescription.getReturnType().asErasure()));
				initializers.add(MethodVariableAccess.REFERENCE.loadFrom(1));
				MethodDescription.InDefinedShape lambda = new MethodDescription.Latent(target,
						"init_" + methodDescription.getName(), Modifier.PRIVATE | Modifier.STATIC,
						Collections.emptyList(), methodDescription.getReturnType().asRawType(),
						Collections.singletonList(new ParameterDescription.Token(
								new TypeDescription.ForLoadedType(BeanFactory.class).asGenericType())),
						Collections.emptyList(), Collections.emptyList(), null, null);
				initializers.add(MethodInvocation.invoke(lambdaMeta).dynamic("get",
						new TypeDescription.ForLoadedType(Supplier.class),
						Collections.singletonList(new TypeDescription.ForLoadedType(BeanFactory.class)),
						Arrays.asList(JavaConstant.MethodType.of(get).asConstantPoolValue(),
								JavaConstant.MethodHandle.of(lambda).asConstantPoolValue(),
								JavaConstant.MethodType
										.of(methodDescription.getReturnType().asErasure(), Collections.emptyList())
										.asConstantPoolValue())));
				initializers.add(ArrayFactory
						.forType(new TypeDescription.ForLoadedType(BeanDefinitionCustomizer.class).asGenericType())
						.withValues(Collections.emptyList()));
				initializers.add(MethodInvocation.invoke(registerBeanWithSupplier));
			}

			initializers.add(MethodReturn.VOID);

			// Create the initialize() method
			builder = builder.method(named("initialize").and(isDeclaredBy(ApplicationContextInitializer.class)))
					.intercept(new Implementation.Simple(new ByteCodeAppender.Simple(initializers)));

			return builder.make();
		}

	}

}
