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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author Dave Syer
 *
 */
public class ModuleSpecs {

	private Types types;
	private Elements elements;

	private Set<InitializerSpec> initializers = new HashSet<>();
	private Map<String, ModuleSpec> modules = new HashMap<>();

	public ModuleSpecs(Types types, Elements elements) {
		this.types = types;
		this.elements = elements;
	}

	public void addInitializer(TypeElement initializer) {
		initializers.add(new InitializerSpec(types, elements, initializer));
	}

	public void addModule(TypeElement module) {
		ModuleSpec value = new ModuleSpec(module);
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
		return modules.values();
	}

}
