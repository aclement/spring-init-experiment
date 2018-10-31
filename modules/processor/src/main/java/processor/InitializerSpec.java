/*
 * Copyright 2016-2017 the original author or authors.
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
package processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

/**
 * @author Dave Syer
 *
 */
public class InitializerSpec {

	private Types types;
	private Elements elements;

	private TypeSpec initializer;
	private String pkg;
	private TypeElement configurationType;

	public InitializerSpec(Types types, Elements elements, TypeElement type) {
		this.types = types;
		this.elements = elements;
		this.configurationType = type;
		this.initializer = createInitializer(type);
		this.pkg = ClassName.get(type).packageName();
	}

	public TypeElement getConfigurationType() {
		return configurationType;
	}

	public void setConfigurationType(TypeElement configurationType) {
		this.configurationType = configurationType;
	}

	public TypeSpec getInitializer() {
		return initializer;
	}

	public void setInitializer(TypeSpec module) {
		this.initializer = module;
	}

	public String getPackage() {
		return pkg;
	}

	public void setPackage(String pkg) {
		this.pkg = pkg;
	}

	private TypeSpec createInitializer(TypeElement type) {
		ClassName className = ClassName.get(type)
				.peerClass(ClassName.get(type).simpleName() + "Initializer");
		Builder builder = TypeSpec.classBuilder(className);
		builder.addModifiers(type.getModifiers().toArray(new Modifier[0]));
		builder.addSuperinterface(SpringClassNames.INITIALIZER_TYPE);
		builder.addMethod(createInitializer());
		builder.addAnnotation(initializerMappingAnnotation());
		return builder.build();
	}

	private AnnotationSpec initializerMappingAnnotation() {
		return AnnotationSpec.builder(SpringClassNames.INITIALIZER_MAPPING)
				.addMember("value", "$T.class", configurationType).build();
	}

	private MethodSpec createInitializer() {
		MethodSpec.Builder builder = MethodSpec.methodBuilder("initialize");
		builder.addAnnotation(Override.class);
		builder.addModifiers(Modifier.PUBLIC);
		builder.addParameter(SpringClassNames.GENERIC_APPLICATION_CONTEXT, "context");
		addBeanMethods(builder, configurationType);
		return builder.build();
	}

	private void addBeanMethods(MethodSpec.Builder builder, TypeElement type) {
		// TODO: pick out the constructor more carefully
		builder.addStatement("context.registerBean($T.class, () -> new $T())", type,
				type);
		for (ExecutableElement method : getBeanMethods(type)) {
			createBeanMethod(builder, method, type);
		}
	}

	private void createBeanMethod(MethodSpec.Builder builder,
			ExecutableElement beanMethod, TypeElement type) {

		String parameterVariables = getParameters(beanMethod, this::parameter)
				.collect(Collectors.joining(", "));

		Object[] parameterTypes = getParameters(beanMethod,
				p -> TypeName.get(types.erasure(p.asType()))).toArray();
		Object[] args = new Object[parameterTypes.length + 1];
		System.arraycopy(parameterTypes, 0, args, 1, parameterTypes.length);
		args[0] = returnType(beanMethod, beanMethod.getReturnType());

		builder.addStatement("context.registerBean(" + "\"" + beanMethod.getSimpleName()
				+ "\", $T.class, " + supplier(type, beanMethod, parameterVariables) + ")",
				args);
	}

	private String supplier(TypeElement owner, ExecutableElement beanMethod,
			String parameterVariables) {
		boolean exception = false;
		TypeMirror exceptionType = elements.getTypeElement(Exception.class.getName())
				.asType();
		TypeMirror runtimeExceptionType = elements
				.getTypeElement(RuntimeException.class.getName()).asType();
		for (TypeMirror type : beanMethod.getThrownTypes()) {
			if (types.isSubtype(type, exceptionType)
					&& !types.isSubtype(type, runtimeExceptionType)) {
				exception = true;
			}
		}
		String wrapper = owner.getSimpleName().toString();
		String code = "context.getBean(" + wrapper + ".class)."
				+ beanMethod.getSimpleName() + "("
				+ (parameterVariables.isEmpty() ? "" : parameterVariables) + ")";
		if (exception) {
			return "() -> { try { return " + code
					+ "; } catch (Exception e) { throw new IllegalStateException(e); } }";
		}
		return "() -> " + code;
	}

	private TypeMirror returnType(ExecutableElement beanMethod, TypeMirror type) {
		if (types.asElement(type).getModifiers().contains(Modifier.PRIVATE)) {
			// Hack, hack, hackety, hack...
			for (TypeMirror subtype : types.directSupertypes(type)) {
				Element element = types.asElement(subtype);
				// Find an interface, any interface...
				if (element.getModifiers().contains(Modifier.PUBLIC)
						&& element.getKind() == ElementKind.INTERFACE) {
					return types.erasure(subtype);
				}
			}
		}
		return types.erasure(type);
	}

	private String parameter(VariableElement p, int i) {
		if (types.asElement(p.asType()).getSimpleName().toString()
				.equals("ObjectProvider")) {
			return "context.getProvider($T.class)";
		}
		return "context.getBean($T.class)";
	}

	private <T> Stream<T> getParameters(ExecutableElement method,
			Function<VariableElement, T> mapper) {
		return method.getParameters().stream().map(mapper);
	}

	private <T> Stream<T> getParameters(ExecutableElement method,
			BiFunction<VariableElement, Integer, T> mapper) {
		List<? extends VariableElement> params = method.getParameters();
		List<T> result = new ArrayList<>();
		for (int i = 0; i < params.size(); i++) {
			result.add(mapper.apply(params.get(i), i));
		}
		return result.stream();
	}

	private List<ExecutableElement> getBeanMethods(TypeElement type) {
		Set<Name> seen = new HashSet<>();
		List<ExecutableElement> beanMethods = new ArrayList<>();
		while (type != null) {
			for (ExecutableElement candidate : ElementFilter
					.methodsIn(type.getEnclosedElements())) {
				if (isBeanMethod(candidate) && seen.add(candidate.getSimpleName())) {
					beanMethods.add(candidate);
				}
			}
			type = getSuperType(type);
		}
		return beanMethods;
	}

	private boolean isBeanMethod(ExecutableElement element) {
		Set<Modifier> modifiers = element.getModifiers();
		return (isAnnotated(element, SpringClassNames.BEAN)
				&& !modifiers.contains(Modifier.STATIC)
				&& !modifiers.contains(Modifier.PRIVATE));
	}

	private TypeElement getSuperType(TypeElement type) {
		TypeMirror superType = type.getSuperclass();
		return (superType == null ? null : (TypeElement) types.asElement(superType));
	}

	private boolean isAnnotated(Element element, ClassName type) {
		for (AnnotationMirror candidate : element.getAnnotationMirrors()) {
			if (type.equals(ClassName.get(candidate.getAnnotationType()))) {
				return true;
			}
		}
		return false;
	}

}
