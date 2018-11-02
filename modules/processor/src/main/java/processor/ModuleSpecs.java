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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.TypeElement;

/**
 * @author Dave Syer
 *
 */
public class ModuleSpecs {

	private ElementUtils utils;

	private Set<InitializerSpec> initializers = new HashSet<>();
	private Map<String, ModuleSpec> modules = new HashMap<>();

	public ModuleSpecs(ElementUtils utils) {
		this.utils = utils;
	}

	public void addInitializer(TypeElement initializer) {
		initializers.add(new InitializerSpec(this.utils, initializer));
	}

	public void addModule(TypeElement module) {
		ModuleSpec value = new ModuleSpec(this.utils, module);
		modules.put(value.getPackage(), value);
	}

	public Collection<ModuleSpec> getModules() {
		for (InitializerSpec initializer : new HashSet<>(initializers)) {
			String pkg = initializer.getPackage();
			for (String root : modules.keySet()) {
				if (root.equals(pkg) || pkg.startsWith(root + ".")) {
					modules.get(root).addInitializer(initializer);
					initializers.remove(initializer);
				}
			}
		}
		for (InitializerSpec initializer : new HashSet<>(initializers)) {
			String pkg = initializer.getPackage();
			for (String root : findRoots(initializers)) {
				if (root.equals(pkg) || pkg.startsWith(root + ".")) {
					if (!modules.containsKey(root)) {
						modules.put(root, new ModuleSpec(this.utils,
								initializer.getConfigurationType()));
					}
					modules.get(root).addInitializer(initializer);
					initializers.remove(initializer);
				}
			}
		}
		return modules.values();
	}

	private Set<String> findRoots(Set<InitializerSpec> initializers) {
		Set<String> roots = new HashSet<>();
		for (InitializerSpec initializer : initializers) {
			roots.add(initializer.getPackage());
		}
		for (InitializerSpec initializer : initializers) {
			String pkg = initializer.getPackage();
			for (String root : new HashSet<>(roots)) {
				// Remove sub-packages
				if (pkg.startsWith(root + ".")) {
					roots.remove(pkg);
				}
			}
		}
		return roots;
	}

}
