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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.squareup.javapoet.ClassName;

/**
 * @author Andy Clement
 * @author Dave Syer
 */
public class ModuleSpecs {

	private final static String MODULE_MAPPINGS_PATH = "META-INF/"
			+ "module-mapping-metadata.properties";

	private Set<InitializerSpec> initializers = new HashSet<>();
	
	private Map<String, ModuleSpec> modules = new HashMap<>();
	
	// Example: app.main.SampleApplication=app.main.SampleApplication,app.main.SampleConfiguration
	private Map<String, List<String>> moduleMappingInfoFromPreviousBuild = new HashMap<>();

	private ElementUtils utils;

	private Filer filer;

	private Messager messager;

	private Map<TypeElement, TypeElement> registrars;

	public ModuleSpecs(ElementUtils utils, Messager messager, Filer filer, Map<TypeElement, TypeElement> registrars) {
		this.utils = utils;
		this.messager = messager;
		this.filer = filer;
		this.registrars = registrars;
		loadModuleSpecs();
	}

	public void addInitializer(TypeElement initializer) {
		initializers.add(new InitializerSpec(this.utils, initializer, registrars));
	}

	public void addModule(TypeElement module) {
		ModuleSpec value = new ModuleSpec(this.utils, module, registrars);
		modules.put(value.getPackage(), value);
	}

	public Collection<ModuleSpec> getModules() {
		for (InitializerSpec initializer : new HashSet<>(initializers)) {
			String pkg = initializer.getPackage();
			for (String root : modules.keySet()) {
				if (root.equals(pkg)) {
					modules.get(root).addInitializer(initializer);
					initializers.remove(initializer);
				}
			}
		}
		Set<String> roots = findRoots(initializers);
		for (InitializerSpec initializer : new HashSet<>(initializers)) {
			String pkg = initializer.getPackage();
			// Find closest root...
			boolean moduleFound = false;
			String packageToCheck = pkg;
			while (!moduleFound) {
				for (String root : roots) {
					if (root.equals(packageToCheck)) {
						if (!modules.containsKey(root)) {
							modules.put(root, new ModuleSpec(this.utils, findKnownRoot(root), registrars));
						}
						modules.get(root).addInitializer(initializer);
						initializers.remove(initializer);
						moduleFound = true;
					}
				}
				if (!moduleFound) {
					int idx = packageToCheck.lastIndexOf(".");
					if (idx == -1) {
						// crap
						throw new IllegalStateException(
								"couldn't find module home for " + initializer);
					}
					packageToCheck = (idx == -1) ? "" : packageToCheck.substring(0, idx);
				}
			}
		}
		return modules.values();
	}

	/**
	 * Check if the root type of the module is known from the previous build
	 * @param pkg the root package for a module
	 */
	private TypeElement findKnownRoot(String pkg) {
		for (Map.Entry<String,List<String>> existingMapping: moduleMappingInfoFromPreviousBuild.entrySet()) {
			String existingModuleClassName = existingMapping.getKey();
			int idx = existingModuleClassName.lastIndexOf(".");
			if (idx != -1) {
				if (pkg.equals(existingModuleClassName.substring(0,idx))) {
					return utils.asTypeElement(existingModuleClassName);
				}
			}
		}
		return null;
	}

	private Set<String> findRoots(Set<InitializerSpec> initializers) {
		Set<String> roots = new HashSet<>();
		for (InitializerSpec initializer : initializers) {
			roots.add(initializer.getPackage());
		}
		return roots;
	}
	
	public void addConfigurationsReferencedByModuleInPreviousBuild(ModuleSpec module) {
		List<String> previousConfigurationsForModule = moduleMappingInfoFromPreviousBuild.get(module.getRootType().toString());
		messager.printMessage(Kind.NOTE, "existing config found for "+module.getRootType().toString()+" - number of configurations #"+(previousConfigurationsForModule==null?0:previousConfigurationsForModule.size()));
		if (previousConfigurationsForModule != null) {
			for (String previousConfiguration: previousConfigurationsForModule) {
				if (utils.asTypeElement(previousConfiguration) == null) {
					// messager.printMessage(Kind.NOTE, "unable to find "+previousConfiguration+" assuming deleted...");
				} else {
					// messager.printMessage(Kind.NOTE, "existence verified: "+previousConfiguration);
					module.addConfigurationFromPreviousBuild(ClassName.bestGuess(previousConfiguration));
				}
			}
		}
	}

	public void loadModuleSpecs() {
		Properties properties = new Properties();
		try {
			FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "",
					MODULE_MAPPINGS_PATH);
			try (InputStream stream = resource.openInputStream();) {
				properties.load(stream);
			}
			for (Map.Entry<Object, Object> property: properties.entrySet()) {
				String moduleClassName = (String)property.getKey();
				List<String> initializerClassNames = Arrays.asList(((String)property.getValue()).split(","));
				this.moduleMappingInfoFromPreviousBuild.put(moduleClassName, initializerClassNames);
			}
			messager.printMessage(Kind.NOTE, "Loaded "+properties.size()+" existing module definitions");
		}
		catch (IOException e) {
			messager.printMessage(Kind.NOTE, "Cannot load "+MODULE_MAPPINGS_PATH+" (normal on first full build)");
		}
	}
	
	public void saveModuleSpecs() {
		Properties properties = new Properties();
		for (Map.Entry<String, ModuleSpec> modulesEntry: modules.entrySet()) {
			ModuleSpec moduleSpec = modulesEntry.getValue();
			Set<InitializerSpec> moduleInitializerSpecs = moduleSpec.getInitializers();
			String root = moduleSpec.getRootType().toString();
			// The property computed here:
			// some.package.FooModule=some.package.RootConfiguration,some.package.OtherConfiguration
			Set<String> toSave = new TreeSet<>();
			toSave.addAll(moduleInitializerSpecs.stream()
					.map(ispec -> ispec.getConfigurationType().asType().toString())
					.collect(Collectors.toList()));
			toSave.addAll(moduleSpec.getPreviouslyAssociatedConfigurations().stream().map(cn -> cn.toString()).collect(Collectors.toList()));
			properties.setProperty(root, toSave.stream().collect(Collectors.joining(",")));
		}
		try {
			FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "",
					MODULE_MAPPINGS_PATH);
			try (OutputStream stream = resource.openOutputStream();) {
				properties.store(stream, "Created by " + getClass().getName());
			}
		}
		catch (IOException e) {
			messager.printMessage(Kind.NOTE, "Cannot write "+MODULE_MAPPINGS_PATH);
		}
	}

	/**
	 * @return the module (if any) handling a particular configuration
	 */
	public ModuleSpec findModuleHandling(ClassName config) {
		for (Map.Entry<String,ModuleSpec> entry: modules.entrySet()) {
			if (entry.getValue().includesConfiguration(config)) {
				return entry.getValue();
			}
		}
		return null;
	}

}
