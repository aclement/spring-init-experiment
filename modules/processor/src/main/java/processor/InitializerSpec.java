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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

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

	private TypeSpec initializer;
	private String pkg;
	private TypeElement configurationType;
	private ElementUtils utils;

	public InitializerSpec(ElementUtils utils, TypeElement type) {
		this.utils = utils;
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
		boolean conditional = utils.hasAnnotation(type,
				SpringClassNames.CONDITIONAL.toString());
		if (conditional) {
			builder.addStatement(
					"$T conditions = context.getBeanFactory().getBean($T.class)",
					SpringClassNames.CONDITION_SERVICE,
					SpringClassNames.CONDITION_SERVICE);
			builder.beginControlFlow("if (conditions.matches($T.class))", type);
		}
		addAnyEnableConfigurationPropertiesRegistrations(builder, type);
		addNewBeanForConfig(builder, type);
		boolean conditionsAvailable = conditional;
		for (ExecutableElement method : getBeanMethods(type)) {
			conditionsAvailable |= createBeanMethod(builder, method, type,
					conditionsAvailable);
		}
		if (conditional) {
			builder.endControlFlow();
		}
	}

	private void addNewBeanForConfig(MethodSpec.Builder builder, TypeElement type) {
		ExecutableElement constructor = getConstructor(type);
		Parameters params = autowireParamsForMethod(constructor);
		builder.addStatement(
				"context.registerBean($T.class, () -> new $T(" + params.format + "))",
				ArrayUtils.merge(type, type, params.args));
	}

	private void addAnyEnableConfigurationPropertiesRegistrations(
			MethodSpec.Builder builder, TypeElement type) {
		AnnotationMirror enableConfigurationProperties = utils.getAnnotation(type,
				SpringClassNames.ENABLE_CONFIGURATION_PROPERTIES.toString());
		if (enableConfigurationProperties != null) {
			List<TypeElement> configurationPropertyTypes = utils
					.getTypesFromAnnotation(enableConfigurationProperties, "value");
			if (configurationPropertyTypes.size() > 0) {
				// builder.addComment("Register calls for @EnableConfigurationProperties:
				// #$L",configurationPropertyTypes.size());
				for (TypeElement t : configurationPropertyTypes) {
					ExecutableElement constructor = getConstructor(t);
					Parameters params = autowireParamsForMethod(constructor);
					builder.addStatement("context.registerBean($T.class, () -> new $T("
							+ params.format + "))", ArrayUtils.merge(t, t, params.args));
				}
			}
		}
	}

	private boolean createBeanMethod(MethodSpec.Builder builder,
			ExecutableElement beanMethod, TypeElement type, boolean conditionsAvailable) {

		TypeMirror returnType = utils.getReturnType(beanMethod);

		boolean conditional = utils.hasAnnotation(beanMethod,
				SpringClassNames.CONDITIONAL.toString());
		if (conditional) {
			if (!conditionsAvailable) {
				builder.addStatement(
						"$T conditions = context.getBeanFactory().getBean($T.class)",
						SpringClassNames.CONDITION_SERVICE,
						SpringClassNames.CONDITION_SERVICE);
			}
			builder.beginControlFlow("if (conditions.matches($T.class, $T.class))", type,
					utils.erasure(returnType));
		}

		Parameters params = autowireParamsForMethod(beanMethod);

		builder.addStatement("context.registerBean(" + "\"" + beanMethod.getSimpleName()
				+ "\", $T.class, " + supplier(type, beanMethod, params.format) + ")",
				ArrayUtils.merge(utils.erasure(returnType), type, params.args));

		if (conditional) {
			builder.endControlFlow();
		}

		return conditional;

	}

	private Parameters autowireParamsForMethod(ExecutableElement method) {
		List<Parameter> parameterTypes = getParameters(method, this::parameterAccessor)
				.collect(Collectors.toList());

		String format = parameterTypes.stream().map(param -> param.format)
				.collect(Collectors.joining(","));
		Object[] args = parameterTypes.stream().flatMap(param -> param.types.stream())
				.collect(Collectors.toList()).toArray();

		Parameters params = new Parameters();
		params.format = format;
		params.args = args;
		return params;
	}

	private String supplier(TypeElement owner, ExecutableElement beanMethod,
			String parameterVariables) {
		boolean exception = utils.throwsCheckedException(beanMethod);
		String code = "context.getBean($T.class)." + beanMethod.getSimpleName() + "("
				+ parameterVariables + ")";
		if (exception) {
			return "() -> { try { return " + code
					+ "; } catch (Exception e) { throw new IllegalStateException(e); } }";
		}
		return "() -> " + code;
	}

	private Parameter parameterAccessor(VariableElement param) {
		Parameter result = new Parameter();
		TypeMirror paramType = param.asType();
		if (utils.getParameterType(param).contains("ObjectProvider")) {
			result.format = "context.getBeanProvider($T.class)";
			if (paramType instanceof DeclaredType) {
				DeclaredType declaredType = (DeclaredType) paramType;
				List<? extends TypeMirror> args = declaredType.getTypeArguments();
				if (!args.isEmpty()) {
					TypeMirror type = args.iterator().next();
					TypeName value = TypeName.get(utils.erasure(type));
					if (type instanceof DeclaredType
							&& !((DeclaredType) type).getTypeArguments().isEmpty()) {
						// The target type itself is generic. So far we only support one
						// level of generic parameters. Further levels could be supported
						// by adding calls to ResolvableType
						result.format = "context.getBeanProvider($T.forClassWithGenerics($T.class, $T.class))";
						result.types.add(SpringClassNames.RESOLVABLE_TYPE);
						if ("?".equals(value.toString())) {
							result.types.add(TypeName.OBJECT);
						}
						else {
							result.types.add(value);
						}
						type = ((DeclaredType) type).getTypeArguments().iterator().next();
					}
					else if (type instanceof ArrayType) {
						// TODO: something special with an array of generic types?
					}
					result.types.add(value);
				}
			}
		}
		else {
			if (paramType instanceof ArrayType) {
				ArrayType arrayType = (ArrayType) paramType;
				// Really?
				result.format = "context.getBeanProvider($T.class).stream().collect($T.toList()).toArray(new $T[0])";
				result.types
						.add(TypeName.get(utils.erasure(arrayType.getComponentType())));
				result.types.add(TypeName.get(Collectors.class));
				result.types
						.add(TypeName.get(utils.erasure(arrayType.getComponentType())));

			}
			else {
				result.format = "context.getBean($T.class)";
				result.types.add(TypeName.get(utils.erasure(param)));
			}
		}
		return result;
	}

	private <T> Stream<T> getParameters(ExecutableElement method,
			Function<VariableElement, T> mapper) {
		return method.getParameters().stream().map(mapper);
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
			type = utils.getSuperType(type);
		}
		return beanMethods;
	}

	private ExecutableElement getConstructor(TypeElement type) {
		Set<Name> seen = new HashSet<>();
		List<ExecutableElement> methods = new ArrayList<>();
		for (ExecutableElement candidate : ElementFilter
				.constructorsIn(type.getEnclosedElements())) {
			if (seen.add(candidate.getSimpleName())) {
				methods.add(candidate);
			}
		}
		// TODO: pick one that is explciitly autowired?
		return methods.get(0);
	}

	private boolean isBeanMethod(ExecutableElement element) {
		Set<Modifier> modifiers = element.getModifiers();
		return (isAnnotated(element, SpringClassNames.BEAN)
				&& !modifiers.contains(Modifier.STATIC)
				&& !modifiers.contains(Modifier.PRIVATE));
	}

	private boolean isAnnotated(Element element, ClassName type) {
		for (AnnotationMirror candidate : element.getAnnotationMirrors()) {
			if (type.equals(ClassName.get(candidate.getAnnotationType()))) {
				return true;
			}
		}
		return false;
	}

	static class Parameter {
		private String format;
		private List<TypeName> types = new ArrayList<>();
	}

	static class Parameters {
		private String format;
		private Object[] args;
	}

}
