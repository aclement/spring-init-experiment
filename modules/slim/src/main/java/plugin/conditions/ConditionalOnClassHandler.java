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
package plugin.conditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.util.ClassUtils;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationValue;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.NullConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.jar.asm.Label;
import plugin.custom.IfEq;

/**
 * @author Andy Clement
 */
public class ConditionalOnClassHandler extends BaseConditionalHandler {
	public ConditionalOnClassHandler() {
		super(ConditionalOnClass.class);
	}

	@Override
	public boolean accept(AnnotationDescription description) {
		return description.getAnnotationType().represents(ConditionalOnClass.class);
	}

	@Override
	public Collection<? extends StackManipulation> computeStackManipulations(AnnotationDescription annoDescription,
			Object annotatedElement, Label conditionFailsLabel) {
		try {
			List<StackManipulation> code = new ArrayList<>();
			AnnotationValue<?, ?> value = annoDescription.getValue(valueProperty);
			// TODO I would prefer the unresolved references...
			TypeDescription[] classes = (TypeDescription[]) value.resolve();
			for (int i = 0; i < classes.length; i++) {
				TypeDescription clazz = classes[i];
				code.add(new TextConstant(clazz.getName()));
				code.add(NullConstant.INSTANCE);
				// Call ClassUtils.isPresent("com.foo.Bar", null)
				code.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(
						ClassUtils.class.getMethod("isPresent", String.class, ClassLoader.class))));
				code.add(new IfEq(conditionFailsLabel));
			}
			return code;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}