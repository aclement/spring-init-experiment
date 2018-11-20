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
import java.util.Map;
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
import javax.tools.Diagnostic.Kind;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import com.squareup.javapoet.WildcardTypeName;

/**
 * @author Dave Syer
 *
 */
public class InitializerSpec implements Comparable<InitializerSpec> {

	private TypeSpec initializer;
	private String pkg;
	private TypeElement configurationType;
	private ElementUtils utils;
	private ClassName className;
	private ClassName moduleName;
	private Map<TypeElement, TypeElement> registrars;

	public InitializerSpec(ElementUtils utils, TypeElement type,
			Map<TypeElement, TypeElement> registrars) {
		this.utils = utils;
		this.className = toInitializerNameFromConfigurationName(type);
		this.pkg = ClassName.get(type).packageName();
		type = registrars.containsKey(type) ? registrars.get(type) : type;
		this.configurationType = type;
		this.registrars = registrars;
	}

	public TypeElement getConfigurationType() {
		return configurationType;
	}

	public void setModuleName(ClassName moduleName) {
		this.moduleName = moduleName;
	}

	public void setConfigurationType(TypeElement configurationType) {
		this.configurationType = configurationType;
	}

	public TypeSpec getInitializer() {
		if (initializer == null) {
			this.initializer = createInitializer(configurationType);
		}
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
		Builder builder = TypeSpec.classBuilder(getClassName());
		builder.addSuperinterface(SpringClassNames.INITIALIZER_TYPE);
		builder.addModifiers(Modifier.PUBLIC);
		if (registrars.containsValue(type)) {
			builder.addMethod(createSelectorInitializer(type));
		}
		else {
			builder.addMethod(createInitializer());
		}
		builder.addMethod(createConfigurations());
		builder.addAnnotation(moduleMappingAnnotation());
		return builder.build();
	}

	public static ClassName toInitializerNameFromConfigurationName(TypeElement type) {
		return toInitializerNameFromConfigurationName(ClassName.get(type));
	}

	public static ClassName toInitializerNameFromConfigurationName(ClassName type) {
		return ClassName.get(type.packageName(), type.simpleName() + "Initializer");
	}

	// TODO better as a method that returns the class?? means less reflection
	private AnnotationSpec moduleMappingAnnotation() {
		return AnnotationSpec.builder(SpringClassNames.MODULE_MAPPING)
				.addMember("module", "$T.class", moduleName).build();
	}

	private MethodSpec createInitializer() {
		MethodSpec.Builder builder = MethodSpec.methodBuilder("initialize");
		builder.addAnnotation(Override.class);
		builder.addModifiers(Modifier.PUBLIC);
		builder.addParameter(SpringClassNames.GENERIC_APPLICATION_CONTEXT, "context");
		addRegistrarInvokers(builder);
		addBeanMethods(builder, configurationType);
		return builder.build();
	}

	private MethodSpec createSelectorInitializer(TypeElement registrar) {
		MethodSpec.Builder mb = MethodSpec.methodBuilder("initialize");
		mb.addAnnotation(Override.class);
		mb.addModifiers(Modifier.PUBLIC);
		mb.addParameter(SpringClassNames.GENERIC_APPLICATION_CONTEXT, "context");
		// TODO use a service to register the registrar rather than calling
		// registerBeanDefinitions right now (like conditionservice)
		mb.addStatement("$T registrar = new $T()", registrar, registrar);
		// TODO invoke relevant Aware related methods
		mb.addStatement("registrar.registerBeanDefinitions(new $T($T.class),context)",
				SpringClassNames.STANDARD_ANNOTATION_METADATA, registrar);
		MethodSpec ms = mb.build();
		return ms;
	}

	private void addRegistrarInvokers(MethodSpec.Builder builder) {
		// System.out.println("Checking if need registrar invokers whilst building
		// initializer for "+configurationType.toString());
		List<? extends AnnotationMirror> annotationMirrors = configurationType
				.getAnnotationMirrors();
		for (AnnotationMirror am : annotationMirrors) {
			// Looking up something like @EnableBar
			TypeElement element = (TypeElement) am.getAnnotationType().asElement();
			TypeElement registrarInitializer = registrars.get(element);
			if (registrarInitializer != null) {
				// System.out.println("Calling initializer for "+element);
				builder.addStatement("new $T().initialize(context)",
						InitializerSpec.toInitializerNameFromConfigurationName(element));
			}
		}
	}

	/**
	 * Looks like:
	 * 
	 * <pre>
	 * <code>
	 * public static Class configurations() {
	 *   return SecondConfiguration.class;
	 * }
	 * </code>
	 * </pre>
	 * 
	 * Or, if the type is private there will be a forName() call. It is called
	 * <tt>configurations()</tt> as might want to return nested configurations as well as
	 * top level? The returned data here mirrors what is in InitializerMapping annotation.
	 */
	private MethodSpec createConfigurations() {
		MethodSpec.Builder builder = MethodSpec.methodBuilder("configurations");
		builder.addModifiers(Modifier.PUBLIC).addModifiers(Modifier.STATIC);
		builder.returns(ParameterizedTypeName.get(ClassName.get(Class.class),
				WildcardTypeName.subtypeOf(Object.class)));
		if (getConfigurationType().getModifiers().contains(Modifier.PRIVATE)) {
			builder.beginControlFlow("try");
			builder.addStatement(
					"return org.springframework.util.ClassUtils.forName(\"$L\",null)",
					getConfigurationType());
			builder.endControlFlow();
			builder.beginControlFlow("catch (ClassNotFoundException cnfe)");
			builder.addStatement("return null");
			builder.endControlFlow();
		}
		else {
			builder.addStatement("return $T.class", getConfigurationType());
		}
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
		if (type.getModifiers().contains(Modifier.PRIVATE)) {
			// We want to do:
			// context.registerBean(Foo.class, () -> new Foo())
			// BUT Foo is private so we can't refer to it directly from some other source
			// file
			builder.beginControlFlow("try");
			builder.addStatement(
					"context.registerBean(org.springframework.util.ClassUtils.forName(\"$L\",null))",
					type);
			builder.endControlFlow();
			builder.beginControlFlow("catch (ClassNotFoundException cnfe)");
			builder.endControlFlow();
		}
		else {
			ExecutableElement constructor = getConstructor(type);
			Parameters params = autowireParamsForMethod(constructor);
			builder.addStatement(
					"context.registerBean($T.class, () -> new $T(" + params.format + "))",
					ArrayUtils.merge(type, type, params.args));
		}
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
		// TODO will need to handle bean methods in private configs
		try {
			TypeMirror returnType = utils.getReturnType(beanMethod);

			Element returnTypeElement = utils.asElement(returnType);
			if (returnTypeElement.getModifiers().contains(Modifier.PRIVATE)) {
				utils.printMessage(Kind.WARNING,
						"TODO: Unable to generate source for bean method, type involved is private: "
								+ beanMethod.getEnclosingElement() + "." + beanMethod);
				return false;
			}
			boolean conditional = utils.hasAnnotation(beanMethod,
					SpringClassNames.CONDITIONAL.toString());
			if (conditional) {
				if (!conditionsAvailable) {
					builder.addStatement(
							"$T conditions = context.getBeanFactory().getBean($T.class)",
							SpringClassNames.CONDITION_SERVICE,
							SpringClassNames.CONDITION_SERVICE);
				}
				builder.beginControlFlow("if (conditions.matches($T.class, $T.class))",
						type, utils.erasure(returnType));
			}

			Parameters params = autowireParamsForMethod(beanMethod);

			builder.addStatement(
					"context.registerBean(" + "\"" + beanMethod.getSimpleName()
							+ "\", $T.class, " + supplier(type, beanMethod, params.format)
							+ ")",
					ArrayUtils.merge(utils.erasure(returnType), type, params.args));

			if (conditional) {
				builder.endControlFlow();
			}

			return conditional;
		}
		catch (Throwable t) {
			throw new RuntimeException("Problem performing createBeanMethod for method "
					+ type.toString() + "." + beanMethod.toString(), t);
		}
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
		String paramTypename = utils.getParameterType(param);
		if (paramTypename.contains("ObjectProvider")) {
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
		else if (paramTypename.equals(SpringClassNames.APPLICATION_CONTEXT.toString())
				|| paramTypename.equals(
						SpringClassNames.CONFIGURABLE_APPLICATION_CONTEXT.toString())) {
			result.format = "context";
		}
		else if (paramTypename.equals(SpringClassNames.BEAN_FACTORY.toString())
				|| paramTypename.equals(SpringClassNames.LISTABLE_BEAN_FACTORY.toString())
				|| paramTypename.equals(
						SpringClassNames.CONFIGURABLE_LISTABLE_BEAN_FACTORY.toString())) {
			result.format = "context.getBeanFactory()";
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
		// TODO: pick one that is explicitly autowired?
		return methods.get(0);
	}

	private boolean isBeanMethod(ExecutableElement element) {
		Set<Modifier> modifiers = element.getModifiers();
		if (!isAnnotated(element, SpringClassNames.BEAN)) {
			return false;
		}
		if (modifiers.contains(Modifier.PRIVATE)) {
			return false;
		}
		return true;
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

	@Override
	public String toString() {
		return "InitializerSpec:" + configurationType.toString();
	}

	public ClassName getClassName() {
		return className;
	}

	@Override
	public int compareTo(InitializerSpec o) {
		return this.className.compareTo(o.getClassName());
	}

}
