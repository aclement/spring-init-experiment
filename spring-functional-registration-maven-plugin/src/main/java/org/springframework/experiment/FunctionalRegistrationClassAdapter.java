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
package org.springframework.experiment;

// TODO what if initialize already exists? (Use Daves variant subclass option and call super)
// TODO what if @Configuration class already has variant ApplicationContextInitialization? (Use Daves subclass variant option)
// TODO arrays/collections of beans passed to @Bean generators
// TODO improve lambda method names - name clashes between lambda generated methods - need to allow for overloading method names (different params/return types)

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Rewrite an @Configuration class:
 * 
 * <ul>
 * <li>introduce interface ApplicationContextInitializer&lt;GenericApplicationContext&gt;
 * <li>create lambda methods for all @Bean methods
 * <li>create initialize method that registers beans via lambda methods
 * <li>create bridge method from initialize(ConfigurableApplicationContext) to initialize(GenericApplicationContext)
 * </ul>
 * 
 * @author Andy Clement
 */
public class FunctionalRegistrationClassAdapter extends ClassVisitor implements Opcodes {

	// TODO use direct references to the spring classes?
	private final static String ApplicationContextInitializer = "org/springframework/context/ApplicationContextInitializer";
	private final static String GenericApplicationContext = "org/springframework/context/support/GenericApplicationContext";
	private final static String ConfigurableApplicationContext = "org/springframework/context/ConfigurableApplicationContext";
	private final static String BeanDefinitionCustomizer = "org/springframework/beans/factory/config/BeanDefinitionCustomizer";
	private final static String BeanAnnotationTypeL = "Lorg/springframework/context/annotation/Bean;";

	private String classname;

	private List<String> atBeanMethods = new ArrayList<>();

	public FunctionalRegistrationClassAdapter(ClassWriter cw) {
		super(ASM6, cw);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		classname = name;
		interfaces = addInterface(interfaces, ApplicationContextInitializer);
		signature = addInterfaceToGenericSignature(signature);
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		return new AtBeanScanningMethodVisitor(mv, name + desc);
	}

	@Override
	public void visitEnd() {
		for (String atBeanMethod : atBeanMethods) {
			generateLambdaMethodForBeanMethod(atBeanMethod);
		}
		createInitializeBridgeMethod();
		createInitializeMethod();
	}

	/**
	 * Create the bridge method that maps the parameter from
	 * ConfigurableApplicationContext to GenericApplicationContext and calls the
	 * other initialize method.
	 */
	private void createInitializeBridgeMethod() {
		MethodVisitor mv = super.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC | ACC_BRIDGE, "initialize",
				"(L" + ConfigurableApplicationContext + ";)V", null, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(CHECKCAST, GenericApplicationContext);
		mv.visitMethodInsn(INVOKEVIRTUAL, classname, "initialize", "(L" + GenericApplicationContext + ";)V", false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(2, 2);
		mv.visitEnd();
	}

	// Example initialize method we are trying to create:
	// public class SampleConfiguration implements ApplicationContextInitializer<GenericApplicationContext> {
	//   @Bean public Foo foo() { return new Foo(); }
	//   @Bean public Bar bar(Foo foo) { return new Bar(foo); }
	//
	//   public void initialize(GenericApplicationContext context) {
	//     context.registerBean(SampleConfiguration.class);
	//     context.registerBean(Foo.class, () ->
	//       context.getBean(SampleConfiguration.class).foo());
	//     context.registerBean(Bar.class, () ->
	//       context.getBean(SampleConfiguration.class).bar(context.getBean(Foo.class)));
	//   }
	// }
	private void createInitializeMethod() {
		MethodVisitor mv;
		mv = super.visitMethod(ACC_PUBLIC, "initialize", "(L"+GenericApplicationContext+";)V", null, null);

		// Create reusable zero size array of BeanDefinitionCustomizer
		mv.visitInsn(ICONST_0);
		mv.visitTypeInsn(ANEWARRAY, BeanDefinitionCustomizer);
		mv.visitVarInsn(ASTORE, 2);

		// Call context.registerBean(ThisConfigurationClass)
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(Type.getType(lname(classname)));
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, GenericApplicationContext, "registerBean", "(Ljava/lang/Class;[L" + BeanDefinitionCustomizer + ";)V", false);

		Handle metafactoryHandle = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory",
				"metafactory",
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
				false);

		// For each bean method, call registerBean(TheBeanClass, () -> { context.getBean(ThisConfigurationClass).beanMethod() }, emptyBeanDefinitionCustomizerArray)
		for (String atBeanMethod : atBeanMethods) {
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(Type.getType(getReturnType(atBeanMethod)));
			mv.visitVarInsn(ALOAD, 1);
			Handle h = new Handle(Opcodes.H_INVOKESTATIC, classname, toLambdaMethod(atBeanMethod),
					"(Lorg/springframework/context/support/GenericApplicationContext;)" + getReturnType(atBeanMethod),
					false);
			mv.visitInvokeDynamicInsn("get", "(L"+GenericApplicationContext+";)Ljava/util/function/Supplier;",
					metafactoryHandle, new Object[] { Type.getType("()Ljava/lang/Object;"), h,
							Type.getType("()" + getReturnType(atBeanMethod)) });
			mv.visitVarInsn(ALOAD, 2);
			// Call registerBean(class, lambda, emptyBeanDefinitionCustomizerArray)
			mv.visitMethodInsn(INVOKEVIRTUAL, GenericApplicationContext, "registerBean", "(Ljava/lang/Class;Ljava/util/function/Supplier;[L"+BeanDefinitionCustomizer+";)V", false);
		}
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 2);
		mv.visitEnd();
		super.visitEnd();
	}

	// Example of generated lambda method:
	// private static com.acme.Foo $pr_foo(org.springframework.beans.factory.BeanFactory);
	//   descriptor: (Lorg/springframework/beans/factory/BeanFactory;)Lcom/acme/Foo;
	//   flags: ACC_PRIVATE, ACC_STATIC
	//   Code:
	//     stack=2, locals=1, args_size=1
	//     0: aload_0
	//     1: ldc #15 // class com/acme/SampleConfiguration
	//     3: invokeinterface #143, 2 // InterfaceMethod org/springframework/beans/factory/BeanFactory.getBean:(Ljava/lang/Class;)Ljava/lang/Object;
	//     8: checkcast #15 // class com/acme/SampleConfiguration
	//    11: invokevirtual #145 // Method foo:()Lcom/acme/Foo;
	//    14: areturn
	private void generateLambdaMethodForBeanMethod(String atBeanMethod) {
		int paren = atBeanMethod.indexOf("(");
		String name = atBeanMethod.substring(0, paren);
		MethodVisitor mv = super.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, toLambdaMethod(atBeanMethod),
				"(Lorg/springframework/context/support/GenericApplicationContext;)" + getReturnType(atBeanMethod), null,
				null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(Type.getType(lname(classname)));
		mv.visitMethodInsn(INVOKEVIRTUAL, "org/springframework/context/support/GenericApplicationContext", "getBean",
				"(Ljava/lang/Class;)Ljava/lang/Object;", false);
		mv.visitTypeInsn(CHECKCAST, classname);
		// TODO lots more error handling required here for badly formed methods so we don't go bang (illegal signatures shouldn't blow us up)
		// Does the method we need to call have params? If so, grab them via getBean()
		int i = paren + 1;
		while (atBeanMethod.charAt(i) != ')') {
			int nextSemi = atBeanMethod.indexOf(';', i);
			String ldescriptorForParameter = atBeanMethod.substring(i, nextSemi + 1);
			// Need to ask the context for this bean
			mv.visitVarInsn(ALOAD, 0);
			mv.visitLdcInsn(Type.getType(ldescriptorForParameter));
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/springframework/context/support/GenericApplicationContext",
					"getBean", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
			mv.visitTypeInsn(CHECKCAST, ldescriptorForParameter.substring(1, ldescriptorForParameter.length() - 1));
			i = nextSemi + 1;
		}
		mv.visitMethodInsn(INVOKEVIRTUAL, classname, name, atBeanMethod.substring(paren), false);
		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 1);
		mv.visitEnd();
	}

	private String lname(String classname) {
		return "L" + classname + ";";
	}

	private String[] addInterface(String[] interfaces, String newInterface) {
		String[] newInterfaces = new String[interfaces.length + 1];
		System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
		newInterfaces[interfaces.length] = newInterface;
		return newInterfaces;
	}

	private String addInterfaceToGenericSignature(String signature) {
		// TODO too crude
		if (signature == null) {
			return "Ljava/lang/Object;L" + ApplicationContextInitializer + "<L" + GenericApplicationContext + ";>;";
		} else {
			return signature + "Ljava/lang/Object;L" + ApplicationContextInitializer + "<L" + GenericApplicationContext
					+ ";>;";
		}
	}

	// TODO allow for multiple methods with differing parameters
	// Determine a suitable name for the lambda method supporting invocation of the bean method
	private String toLambdaMethod(String atBeanMethod) {
		return "$pr_" + atBeanMethod.substring(0, atBeanMethod.indexOf('('));
	}

	private String getReturnType(String methodWithDescriptor) {
		return methodWithDescriptor.substring(methodWithDescriptor.lastIndexOf(')') + 1);
	}

	/**
	 * Discover if a method is @Bean annotated.
	 */
	class AtBeanScanningMethodVisitor extends MethodVisitor {

		private String methodsignature;

		public AtBeanScanningMethodVisitor(MethodVisitor mv, String methodsignature) {
			super(ASM6, mv);
			this.methodsignature = methodsignature;
		}

		@Override
		public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
			if (descriptor.equals(BeanAnnotationTypeL)) {
				atBeanMethods.add(methodsignature);
			}
			return super.visitAnnotation(descriptor, visible);
		}
	}

}
