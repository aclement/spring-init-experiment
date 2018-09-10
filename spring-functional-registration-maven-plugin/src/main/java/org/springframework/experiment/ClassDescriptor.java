/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.experiment;

import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public class ClassDescriptor {
	
	private final static String ConfigurationClass = "Lorg/springframework/context/annotation/Configuration;";

	private ClassNode node;
	
	private ClassDescriptor(ClassNode node) {
		this.node = node;
	}
	
	static ClassDescriptor forClassBytes(byte[] classBytes) {
		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(classBytes);
		reader.accept(node, ClassReader.SKIP_DEBUG);
		return new ClassDescriptor(node);
	}


	public boolean isConfigurationClass() {
		List<AnnotationNode> visibleAnnotations = node.visibleAnnotations;
		if (visibleAnnotations != null) {
			for (AnnotationNode anno: visibleAnnotations) {
				if (anno.desc.equals(ConfigurationClass)) {
					return true;
				}
			} 
		}
		return false;
	}

	public String getName() {
		return node.name;
	}

}
