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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

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
	private Set<InitializerSpec> initializers = new TreeSet<>();
	private Set<ClassName> previouslyAssociatedConfigurations = new TreeSet<>();
	private boolean processed = false;
	private TypeElement rootType;

	private ElementUtils utils;
	private ImportsSpec imports;

	public ModuleSpec(ElementUtils utils, TypeElement rootType, ImportsSpec imports) {
		if (rootType != null) {
			setRootType(rootType);
		}
		this.utils = utils;
		this.imports = imports;
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

	public Set<ClassName> getPreviouslyAssociatedConfigurations() {
		return previouslyAssociatedConfigurations;
	}

	public void addInitializer(InitializerSpec initializer) {
		this.initializers.add(initializer);
	}

	public ClassName getClassName() {
		return ClassName.get(pkg, module.name);
	}

	public void prepare(ModuleSpecs specs) {
		if (this.processed) {
			return;
		}
		if (this.module == null) {
			findModuleRoot();
		}
		if (this.module != null) {
			specs.addConfigurationsReferencedByModuleInPreviousBuild(this);
			this.processed = true;
		}
	}

	public void produce(ModuleSpecs specs) {
		if (hasNonVisibleConfiguration()) {
			// Use configurations() method
			this.module = module.toBuilder().addMethod(createInitializers())
					.addMethod(createConfigurations()).addMethod(createGetRoot()).build();
		}
		else {
			// Use @Import annotation
			this.module = importAnnotation(specs, module.toBuilder())
					.addMethod(createInitializers()).addMethod(createGetRoot()).build();
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
		Set<Modifier> modifiers = new HashSet<>(type.getModifiers());
		if (modifiers.contains(Modifier.ABSTRACT)) {
			modifiers.remove(Modifier.ABSTRACT);
		}
		if (modifiers.contains(Modifier.PRIVATE)) {
			// Can't make it private, will cause issues
			modifiers.remove(Modifier.PRIVATE);
		}
		if (modifiers.contains(Modifier.STATIC)) {
			// Can't make it private, will cause issues
			modifiers.remove(Modifier.STATIC);
		}
		if (!modifiers.contains(Modifier.PUBLIC)) {
			modifiers.add(Modifier.PUBLIC);
		}
		builder.addModifiers(modifiers.toArray(new Modifier[0]));
		builder.addSuperinterface(SpringClassNames.MODULE);
		return builder.build();
	}

	private MethodSpec createInitializers() {
		MethodSpec.Builder builder = MethodSpec.methodBuilder("initializers");
		builder.addAnnotation(Override.class);
		builder.addModifiers(Modifier.PUBLIC);
		builder.returns(ParameterizedTypeName.get(ClassName.get(List.class),
				SpringClassNames.INITIALIZER_TYPE));

		Set<ClassName> subset = new HashSet<>();
		for (InitializerSpec object : initializers) {
			// This prevents an app from @Importing itself (libraries don't usually do
			// it). We could add another annotation to signal the "module-root" or
			// something, but this seems OK for now.
			if (!object.getConfigurationType().equals(rootType) && imports.getIncluded().contains(object.getConfigurationType())) {
				// It's going to be imported somewhere else
				continue;
			}
			subset.add(object.getClassName());
		}
		subset.addAll(nonSelfImportedPreviouslyAssociatedConfigurations());

		// If this is an incremental build we may just be building 1 initializer (when the
		// module in fact includes multiple)
		utils.printMessage(Kind.NOTE,
				"Creating module initializers() method for " + getClassName() + ": current initializers: "
						+ subset + " previous initializers: "
						+ previouslyAssociatedConfigurations);
		builder.addStatement("return $T.asList(" + newInstances(subset.size()) + ")",
				array(Arrays.class, subset));
		return builder.build();
	}

	// This code is used both in computing the annotation (@Imports) and computing the initializers - are the
	// filter expressions the same in both cases? (tests so far do pass...)
	private List<ClassName> nonSelfImportedPreviouslyAssociatedConfigurations() {
		return previouslyAssociatedConfigurations.stream()
				.filter(cn -> !isSelfImport(utils.asTypeElement(cn.toString())))
				.filter(cn -> !isMyPackageSpace(cn)) // should this check 'imports' instead?
				.collect(Collectors.toList());
	}
	
	private MethodSpec createGetRoot() {
		MethodSpec.Builder builder = MethodSpec.methodBuilder("getRoot");
		builder.addAnnotation(Override.class);
		builder.addModifiers(Modifier.PUBLIC);
		builder.returns(WildcardTypeName.get(Class.class));
		builder.addStatement("return $T.class", rootType);
		return builder.build();
	}

	private MethodSpec createConfigurations() {
		// Want to include the same thing in configurations() method that would be in
		// @Import annotation
		Set<ClassName> subset = new HashSet<>();
		Set<ClassName> imported = new HashSet<>();
		for (InitializerSpec object : initializers) {
			// This prevents an app from @Importing itself (libraries don't usually do
			// it). We could add another annotation to signal the "module-root" or
			// something, but this seems OK for now.
			if (isSelfImport(object.getConfigurationType())) {
				continue;
			}
			subset.add(object.getClassName());
			if (imports.getImports().containsKey(object.getConfigurationType())) {
				for (TypeElement item : imports.getImports()
						.get(object.getConfigurationType())) {
					imported.add(ClassName.get(item));
				}
			}
		}
		// Only import them once
		subset.removeAll(imported);
		subset.addAll(nonSelfImportedPreviouslyAssociatedConfigurations());
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

	private TypeSpec.Builder importAnnotation(ModuleSpecs specs, TypeSpec.Builder type) {
		ClassName[] array = findImports(initializers);
		array = convertImportsToModules(specs, array);
		if (array.length == 0) {
			return type;
		}
		AnnotationSpec.Builder builder = AnnotationSpec.builder(SpringClassNames.IMPORT);
		builder.addMember("value",
				array.length > 1 ? ("{" + typeParams(array.length) + "}") : "$T.class",
				(Object[]) array);
		return type.addAnnotation(builder.build());
	}

	private ClassName[] convertImportsToModules(ModuleSpecs specs, ClassName[] array) {
		System.out.println("Producing module " + this.getClassName());
		Set<ClassName> newImports = new LinkedHashSet<>();
		for (ClassName o : array) {
			// If another module in this build is handling the configuration, switch to
			// refer to it
			ModuleSpec spec = specs.findModuleHandling(o);
			if (spec != null) {
				if (spec != null && spec != this) {
					System.out.println("Modifying autoconfig reference for module "
							+ this.getClassName() + ": changing from " + o + " to "
							+ spec.getClassName());
					newImports.add(ClassName.bestGuess(spec.getClassName().toString()));
				}
				else {
					newImports.add(o);
				}
			}
			else {
				System.out.println("Considering " + o.toString());
				TypeElement initializerTypeElement = utils
						.asTypeElement(o.toString() + "Initializer");
				boolean rewritten = false;
				if (initializerTypeElement != null) {
					List<? extends AnnotationMirror> annotationMirrors = initializerTypeElement
							.getAnnotationMirrors();
					for (AnnotationMirror am : annotationMirrors) {
						if (am.getAnnotationType().asElement().getSimpleName().toString()
								.contains("ModuleMapping")) {
							TypeElement responsibleModule = utils
									.getTypesFromAnnotation(am, "module").get(0);
							System.out
									.println("Modifying autoconfig reference for module "
											+ this.getClassName() + ": changing from " + o
											+ " to " + responsibleModule);
							newImports.add(ClassName.get(responsibleModule));// ClassName.get(responsibleModule));
							rewritten = true;
						}
					}
				}
				if (!rewritten) {
					System.out.println(
							"Problem? Unable to find module responsible for configuration "
									+ o.toString() + " whilst building module "
									+ this.getClassName());
					newImports.add(o);
				}
			}
		}
		ClassName[] result = newImports.toArray(new ClassName[0]);
		System.out.println("Arrays from " + Arrays.toString(array) + " to "
				+ Arrays.toString(result));
		return result;
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

	private boolean isMyPackageSpace(ClassName cn) {
		return cn.toString().startsWith(pkg);
	}

	private ClassName[] findImports(Collection<InitializerSpec> collection) {
		List<TypeElement> types = new ArrayList<>();
		for (TypeElement imported : imports.getImports(rootType)) {
			if (!imported.getQualifiedName().toString().startsWith(pkg)) {
				types.add(imported);
			}
		}
		Set<ClassName> list = new LinkedHashSet<>();
		collectImports(types, list);
		list = removeImportSelectorsAndRegistrars(list);
		return list.toArray(new ClassName[0]);
	}

	private Set<ClassName> removeImportSelectorsAndRegistrars(Set<ClassName> list) {
		Set<ClassName> result = new HashSet<>();
		for (ClassName cn : list) {
			TypeElement te = utils.asTypeElement(cn.toString());
			if (utils.implementsInterface(te,
					SpringClassNames.IMPORT_BEAN_DEFINITION_REGISTRAR)) {
				System.out.println("Skipping inclusion of registrar " + te
						+ " when building " + this.getClassName());
			}
			else if (utils.implementsInterface(te, SpringClassNames.IMPORT_SELECTOR)) {
				System.out.println("Skipping inclusion of import selector " + te
						+ " when building " + this.getClassName());
			}
			else {
				result.add(cn);
			}
		}
		return result;
	}

	private void collectImports(List<TypeElement> types, Collection<ClassName> list) {
		for (TypeElement type : types) {
			if (!isSelfImport(type)) {
				list.add(ClassName.get(type));
			}
		}
		list.addAll(nonSelfImportedPreviouslyAssociatedConfigurations());
	}

	private boolean isSelfImport(TypeElement s) {
		AnnotationMirror imported = utils.getAnnotation(rootType,
				SpringClassNames.IMPORT.toString());
		if (imported == null) {
			return false;
		}
		if (utils.findTypeInAnnotation(imported, "value", getClassName().toString())) {
			return true;
		}
		return rootType.getQualifiedName().equals(s.getQualifiedName());
	}

	private Object[] array(Object first, Collection<ClassName> collection) {
		Object[] array = new Object[collection.size() + 1];
		array[0] = first;
		int i = 1;
		for (ClassName object : collection) {
			array[i++] = object;
		}
		return array;
	}

	/**
	 * Add an configuration type known from a previous APT run that still exists and
	 * should be included when this module is output.
	 */
	public boolean addConfigurationFromPreviousBuild(
			ClassName previousExistingConfigurationClassName) {
		boolean exists = initializers.stream()
				.anyMatch(ispec -> ispec.getConfigurationType().toString()
						.equals(previousExistingConfigurationClassName.toString()));
		if (!exists) {
			return previouslyAssociatedConfigurations
					.add(previousExistingConfigurationClassName);
		}
		return false;
	}

	public boolean includesConfiguration(ClassName config) {
		for (InitializerSpec spec : initializers) {
			if (ClassName.get(spec.getConfigurationType()).equals(config)) {
				return true;
			}
		}
		for (ClassName cn : previouslyAssociatedConfigurations) {
			// TODO yuck
			if (config.toString().equals(cn.toString() + "Initializer")) {
				return true;
			}
		}
		return false;
	}

}
