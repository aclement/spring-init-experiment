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

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationValue;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Label;
import plugin.Methods;
import plugin.Types;
import plugin.custom.ArrayLength;
import plugin.custom.IfNe;

/**
 * @author Andy Clement
 */
public class ConditionalOnMissingBeanHandler extends BaseConditionalHandler {

	public ConditionalOnMissingBeanHandler() {
		super(Types.ConditionalOnMissingBean());
	}

	@Override
	public boolean accept(AnnotationDescription description) {
		return description.getAnnotationType().equals(Types.ConditionalOnMissingBean());
	}

	@Override
	public Collection<? extends StackManipulation> computeStackManipulations(AnnotationDescription annoDescription,
			Object annotatedElement, Label conditionFailsLabel) {
		try {
			List<StackManipulation> code = new ArrayList<>();
			AnnotationValue<?, ?> value = annoDescription.getValue(valueProperty);
			TypeDescription[] conditionalTypes = (TypeDescription[]) value.resolve();
			// What to call: if
			// (context.getBeanFactory().getBeanNamesForType(Gson.class).length == 0)
			code.add(MethodVariableAccess.REFERENCE.loadFrom(1)); // Load context
			code.add(MethodInvocation.invoke(Methods.getBeanFactory()));
			TypeDescription beanType = null;
			// TODO [loose end] Support specifying types in annotation and not using method return
			if (annotatedElement instanceof MethodDescription.InDefinedShape) {
				beanType = ((MethodDescription.InDefinedShape) annotatedElement)
						.getReturnType().asErasure();
			} else {
				// TODO [loose end] Support multiple types specified
				beanType = conditionalTypes[0];
			}
			code.add(ClassConstant.of(beanType));
			code.add(MethodInvocation.invoke(Methods.getBeanNamesForType()));
			code.add(new ArrayLength());
			code.add(new IfNe(conditionFailsLabel));
			return code;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}