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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

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
			this.module = importAnnotation(module.toBuilder())
					.addMethod(createInitializers()).build();
			this.processed = true;
		}
	}

	private void findModuleRoot() {
		Set<InitializerSpec> candidates = new HashSet<>();
		String pkg = null;
		for (InitializerSpec initializer : initializers) {
			// Only consider configuration that is not an inner (static) class
			if (!initializer.getConfigurationType().getModifiers()
					.contains(Modifier.STATIC)) {
				if (pkg == null) {
					pkg = initializer.getPackage();
				}
				else if (pkg.length() > initializer.getPackage().length()) {
					// Choose only candidates from the top level package
					pkg = initializer.getPackage();
					candidates.clear();
				}
				candidates.add(initializer);
			}
		}
		if (candidates.size() > 1) {
			for (InitializerSpec initializer : candidates) {
				// If there are multiple candidates, we prefer one that is
				// "AutoConfiguration"
				if (initializer.getConfigurationType().getQualifiedName().toString()
						.endsWith("AutoConfiguration")) {
					setRootType(initializer.getConfigurationType());
					break;
				}
			}
		}
		else if (candidates.size() == 1) {
			setRootType(candidates.iterator().next().getConfigurationType());
		}
		else {
			// Fast fail
			throw new IllegalStateException("No root type could be determined for module "+this.pkg+" from these initializers: "+initializers);
		}
	}

	private TypeSpec createModule(TypeElement type) {
		ClassName className = ClassName.get(type)
				.peerClass(ClassName.get(type).simpleName() + "Module");
		Builder builder = TypeSpec.classBuilder(className);
		builder.addModifiers(type.getModifiers().toArray(new Modifier[0]));
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
			array[i++] = ClassName.bestGuess(object.getInitializer().name);
		}
		return array;
	}

}
