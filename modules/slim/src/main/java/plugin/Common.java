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

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ImplementationDefinition;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.TypeCreation;
import net.bytebuddy.implementation.bytecode.constant.NullConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;

public class Common {

	/**
	 * Generate code that will print the specified string. This code can then be
	 * inserted into a type as it is built and will execute at runtime to print the
	 * string. (The generated code will run <tt>System.out.println(string)</tt>.
	 * 
	 * @param string the message to be shown when the code is run.
	 * @return generated code to print the message
	 */
	static Collection<? extends StackManipulation> generateCodeToPrintln(String string) {
		List<StackManipulation> code = new ArrayList<>();
		net.bytebuddy.description.field.FieldDescription.InDefinedShape sysoutfield = new TypeDescription.ForLoadedType(
				System.class).getDeclaredFields().filter(em -> em.getActualName().equals("out")).get(0);
		code.add(FieldAccess.forField(sysoutfield).read());
		code.add(new TextConstant(string));
		InDefinedShape printlnMethod = new TypeDescription.ForLoadedType(PrintStream.class).getDeclaredMethods()
				.filter(em -> em.getActualName().equals("println") && em.toString().contains("String")).get(0);
		code.add(MethodInvocation.invoke(printlnMethod));
		return code;
	}

	static DynamicType.Builder<?> addInitializerMethod(DynamicType.Builder<?> builder, DynamicType initializerClassType) {
		return addInitializerMethod(builder, initializerClassType, null);
	}

	static DynamicType.Builder<?> addInitializerMethod(DynamicType.Builder<?> builder, DynamicType initializerClassType,
			String nameInsert) {
		return addInitializerMethod(builder, initializerClassType.getTypeDescription(), nameInsert);
	}

	/**
	 * Create $$initializer method in configuration class:
	 * 
	 * <pre>
	 * <code>
	 *  public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
	 *    return new SampleConfiguration.$Initializer();
	 *  }
	 *  </code>
	 * 
	 * <pre>
	 */
	static DynamicType.Builder<?> addInitializerMethod(DynamicType.Builder<?> builder,
			TypeDescription initializerClassTypeDescription, String name) {
//		TypeDescription initializerClassTypeDescription = initializerClassType.getTypeDescription();
		InDefinedShape inDefinedShape = initializerClassTypeDescription.getDeclaredMethods().filter((em) -> em.isConstructor())
				.get(0);
		List<StackManipulation> code = new ArrayList<>();
		code.addAll(Common
				.generateCodeToPrintln(":debug: $$initializer method running for " + initializerClassTypeDescription.getActualName()));
		code.add(TypeCreation.of(initializerClassTypeDescription));
		code.add(Duplication.SINGLE);
		code.add(NullConstant.INSTANCE);
		code.add(MethodInvocation.invoke(inDefinedShape));
		code.add(MethodReturn.of(Types.ParameterizedApplicationContextInitializerWithGenericApplicationContext()));
		ImplementationDefinition<?> method = builder.defineMethod(name == null ? "$$initializer" : name,
				Types.ParameterizedApplicationContextInitializerWithGenericApplicationContext(), net.bytebuddy.description.modifier.Visibility.PUBLIC,
				Ownership.STATIC);
		builder = method.intercept(new Implementation.Simple(new ByteCodeAppender.Simple(code)));
		return builder;
	}


	static AnnotationDescription findAnnotation(TypeDescription td, Class<?> c) {
		AnnotationList annotations = td.getDeclaredAnnotations();
		for (AnnotationDescription annotation : annotations) {
			if (annotation.getAnnotationType().represents(c)) {
				return annotation;
			}
		}
		return null;
	}

	static AnnotationDescription findAnnotation(TypeDescription td, TypeDescription c) {
		AnnotationList annotations = td.getDeclaredAnnotations();
		for (AnnotationDescription annotation : annotations) {
			if (annotation.getAnnotationType().equals(c)) {
				return annotation;
			}
		}
		return null;
	}


	static boolean hasAnnotation(TypeDescription target, Class<? extends Annotation> annotation) {
		return target.getDeclaredAnnotations().stream().anyMatch(desc -> isMetaAnnotated(desc, annotation));
	}

	static boolean hasAnnotation(TypeDescription target, TypeDescription annotation) {
		return target.getDeclaredAnnotations().stream().anyMatch(desc -> isMetaAnnotated(desc, annotation));
	}

	static boolean isMetaAnnotated(AnnotationDescription desc, TypeDescription annotation) {
		return findMetaAnnotation(desc, annotation) != null;
	}

	static boolean isMetaAnnotated(AnnotationDescription desc, Class<? extends Annotation> annotation) {
		return findMetaAnnotation(desc, annotation) != null;
	}

	static AnnotationDescription findMetaAnnotation(AnnotationDescription desc, Class<? extends Annotation> annotation) {
		return findMetaAnnotation(desc, annotation, new HashSet<>());
	}

	static AnnotationDescription findMetaAnnotation(AnnotationDescription desc, TypeDescription annotation) {
		return findMetaAnnotation(desc, annotation, new HashSet<>());
	}

	static AnnotationDescription findMetaAnnotation(AnnotationDescription desc, Class<? extends Annotation> annotation,
			Set<AnnotationDescription> seen) {
//		log("Searching for " + annotation + " in " + desc);
		TypeDescription type = desc.getAnnotationType();
		seen.add(desc);
		if (type.represents(annotation)) {
			return desc;
		}
		for (AnnotationDescription ann : type.getDeclaredAnnotations()) {
			if (!seen.contains(ann)) {
				AnnotationDescription found = findMetaAnnotation(ann, annotation, seen);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	static AnnotationDescription findMetaAnnotation(AnnotationDescription desc, TypeDescription annotation,
			Set<AnnotationDescription> seen) {
//		log("Searching for " + annotation + " in " + desc);
		TypeDescription type = desc.getAnnotationType();
		seen.add(desc);
		if (type.equals(annotation)) {
			return desc;
		}
		for (AnnotationDescription ann : type.getDeclaredAnnotations()) {
			if (!seen.contains(ann)) {
				AnnotationDescription found = findMetaAnnotation(ann, annotation, seen);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	public static boolean isPresent(String className, ClassLoader classLoader) {
		try {
			Class.forName(className, false, classLoader);
			return true;
		}
		catch (Throwable ex) {
			// Class or one of its dependencies is not present...
			return false;
		}
	}
}
