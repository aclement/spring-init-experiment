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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import com.squareup.javapoet.WildcardTypeName;

/**
 * @author Dave Syer
 *
 */
public class ModuleSpec {

	private TypeSpec module;
	private String pkg;
	private Set<InitializerSpec> initializers = new HashSet<>();
	private boolean processed = false;
	private TypeElement rootType;

	private ElementUtils utils;

	public ModuleSpec(ElementUtils utils) {
		this(utils, null);
	}

	public ModuleSpec(ElementUtils utils, TypeElement type) {
		if (type != null) {
			setRootType(type);
		}
		this.utils = utils;
	}

	public TypeElement getRootType() {
		return rootType;
	}

	public void setRootType(TypeElement type) {
		this.rootType = type;
		this.module = createModule(type);
		this.pkg = ClassName.get(type).packageName();
	}

	public TypeSpec getModule() {
		return module;
	}

	public String getPackage() {
		return pkg;
	}

	public Set<InitializerSpec> getInitializers() {
		return initializers;
	}

	public void addInitializer(InitializerSpec initializer) {
		this.initializers.add(initializer);
	}

	public String getClassName() {
		return ClassName.get(pkg, module.name).toString();
	}

	public void process() {
		if (this.processed) {
			return;
		}
		if (this.module == null) {
			findModuleRoot();
		}
		if (this.module != null) {
			findNestedInitializers();
			if (hasNonVisibleConfiguration()) {
				// Use configurations() method
				this.module = module.toBuilder().addMethod(createInitializers())
						.addMethod(createConfigurations()).build();
			}
			else {
				// Use @Import annotationn
				this.module = importAnnotation(module.toBuilder())
						.addMethod(createInitializers()).build();
			}
			this.processed = true;
		}
	}

	private boolean hasNonVisibleConfiguration() {
		for (InitializerSpec initializer : initializers) {
			if (initializer.getConfigurationType().getModifiers()
					.contains(Modifier.PRIVATE)) {
				return true;
			}
		}
		return false;
	}

	private void findNestedInitializers() {
		Set<TypeElement> candidates = new HashSet<>();
		for (InitializerSpec initializer : initializers) {
			findNestedInitializers(initializer.getConfigurationType(), candidates);
		}
		Set<TypeElement> roots = new HashSet<>();
		for (InitializerSpec initializer : initializers) {
			roots.add(initializer.getConfigurationType());
		}
		candidates.removeAll(roots);
		for (TypeElement candidate : candidates) {
			addInitializer(new InitializerSpec(utils, candidate));
		}

	}

	private void findNestedInitializers(TypeElement type, Set<TypeElement> types) {
		if (type.getKind() == ElementKind.CLASS
				&& !type.getModifiers().contains(Modifier.ABSTRACT)
				&& utils.hasAnnotation(type, SpringClassNames.CONFIGURATION.toString())) {
			types.add(type);
			for (Element element : type.getEnclosedElements()) {
				if (element instanceof TypeElement
						&& element.getModifiers().contains(Modifier.STATIC)) {
					findNestedInitializers((TypeElement) element, types);
				}
			}
		}

	}

	private void findModuleRoot() {
		Set<InitializerSpec> candidates = new HashSet<>();
		for (InitializerSpec initializer : initializers) {
			if (utils.hasAnnotation(initializer.getConfigurationType(),
					SpringClassNames.MODULE_ROOT.toString())) {
				candidates.add(initializer);
			}
		}
		if (candidates.isEmpty()) {
			for (InitializerSpec initializer : initializers) {
				// If there are multiple candidates, we prefer one that is
				// "AutoConfiguration"
				if (initializer.getConfigurationType().getQualifiedName().toString()
						.endsWith("AutoConfiguration")) {
					candidates.add(initializer);
					break;
				}
			}
		}
		if (candidates.size() >= 1) {
			// TODO: remove random choice
			setRootType(candidates.iterator().next().getConfigurationType());

		}
		else if (initializers.size() >= 1) {
			// TODO: remove random choice
			setRootType(initializers.iterator().next().getConfigurationType());
		}
		else {
			// Fast fail
			throw new IllegalStateException("No root type could be determined for module "
					+ "from these initializers: " + initializers);
		}
	}

	private TypeSpec createModule(TypeElement type) {
		ClassName className = ClassName.get(type)
				.peerClass(ClassName.get(type).simpleName() + "Module");
		Builder builder = TypeSpec.classBuilder(className);
		Set<Modifier> modifiers = type.getModifiers();
		if (!modifiers.contains(Modifier.PRIVATE)) {
			// Can't make it private, will cause issues
			builder.addModifiers(modifiers.toArray(new Modifier[0]));
		}
		builder.addSuperinterface(SpringClassNames.MODULE);
		return builder.build();
	}

	private MethodSpec createInitializers() {
		MethodSpec.Builder builder = MethodSpec.methodBuilder("initializers");
		builder.addAnnotation(Override.class);
		builder.addModifiers(Modifier.PUBLIC);
		builder.returns(ParameterizedTypeName.get(ClassName.get(List.class),
				SpringClassNames.INITIALIZER_TYPE));
		builder.addStatement(
				"return $T.asList(" + newInstances(initializers.size()) + ")",
				array(Arrays.class, initializers));
		return builder.build();
	}

	private MethodSpec createConfigurations() {
		// Want to include the same thing in configurations() method that would be in
		// @Import annotation
		List<InitializerSpec> subset = new ArrayList<>();
		for (InitializerSpec object : initializers) {
			// This prevents an app from @Importing itself (libraries don't usually do
			// it). We could add another annotation to signal the "module-root" or
			// something, but this seems OK for now.
			if (isSelfImport(object)) {
				continue;
			}
			subset.add(object);
		}

		MethodSpec.Builder builder = MethodSpec.methodBuilder("configurations");
		builder.addAnnotation(Override.class);
		builder.addModifiers(Modifier.PUBLIC);
		builder.returns(ParameterizedTypeName.get(ClassName.get(List.class),
				ParameterizedTypeName.get(ClassName.get(Class.class),
						WildcardTypeName.subtypeOf(Object.class))));
		builder.addStatement(
				"return $T.asList(" + queryConfigurations(subset.size()) + ")",
				array(Arrays.class, subset));
		return builder.build();
	}

	private TypeSpec.Builder importAnnotation(TypeSpec.Builder type) {
		Object[] array = types(initializers);
		if (array.length == 0) {
			return type;
		}
		AnnotationSpec.Builder builder = AnnotationSpec.builder(SpringClassNames.IMPORT);
		builder.addMember("value",
				array.length > 1 ? ("{" + typeParams(array.length) + "}") : "$T.class",
				array);
		return type.addAnnotation(builder.build());
	}

	private String queryConfigurations(int count) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			if (builder.length() > 0) {
				builder.append(", ");
			}
			builder.append("$T.configurations()");
		}
		return builder.toString();
	}

	private String newInstances(int count) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			if (builder.length() > 0) {
				builder.append(", ");
			}
			builder.append("new $T()");
		}
		return builder.toString();
	}

	private String typeParams(int count) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			if (builder.length() > 0) {
				builder.append(", ");
			}
			builder.append("$T.class");
		}
		return builder.toString();
	}

	private Object[] types(Collection<InitializerSpec> collection) {
		List<Object> list = new ArrayList<>();
		for (InitializerSpec object : collection) {
			// This prevents an app from @Importing itself (libraries don't usually do
			// it). We could add another annotation to signal the "module-root" or
			// something, but this seems OK for now.
			if (isSelfImport(object)) {
				continue;
			}
			list.add(ClassName.get(object.getConfigurationType()));
		}
		return list.toArray(new Object[0]);
	}

	private boolean isSelfImport(InitializerSpec initializer) {
		AnnotationMirror imported = utils.getAnnotation(rootType,
				SpringClassNames.IMPORT.toString());
		if (imported == null) {
			return false;
		}
		if (utils.findTypeInAnnotation(imported, "value", getClassName())) {
			return true;
		}
		return rootType.getQualifiedName()
				.equals(initializer.getConfigurationType().getQualifiedName());
	}

	private Object[] array(Object first, Collection<InitializerSpec> collection) {
		Object[] array = new Object[collection.size() + 1];
		array[0] = first;
		int i = 1;
		for (InitializerSpec object : collection) {
			array[i++] = object.getClassName();
		}
		return array;
	}

}
