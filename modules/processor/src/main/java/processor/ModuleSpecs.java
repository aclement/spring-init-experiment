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
							modules.put(root, new ModuleSpec(this.utils));
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

	private Set<String> findRoots(Set<InitializerSpec> initializers) {
		Set<String> roots = new HashSet<>();
		for (InitializerSpec initializer : initializers) {
			roots.add(initializer.getPackage());
		}
		return roots;
	}

}
