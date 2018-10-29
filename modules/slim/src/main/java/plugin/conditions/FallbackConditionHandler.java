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
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Label;
import plugin.Methods;
import plugin.custom.IfEq;

/**
 * @author Andy Clement
 */
public class FallbackConditionHandler implements ConditionalHandler {

	@Override
	public boolean accept(AnnotationDescription description) {
		return true;
	}

	@Override
	public Collection<? extends StackManipulation> computeStackManipulations(AnnotationDescription annoDescription,
			Object annotatedElement, Label conditionFailsLabel) {
		try {
			List<StackManipulation> code = new ArrayList<>();
			if (annotatedElement instanceof MethodDescription) {
				// Call ConditionService.matches(ConfigurationClass, BeanClass)
				code.add(MethodVariableAccess.REFERENCE.loadFrom(3));
				code.add(ClassConstant.of(((MethodDescription) annotatedElement).getDeclaringType().asErasure()));
				code.add(ClassConstant.of(((MethodDescription) annotatedElement).getReturnType().asErasure()));
				code.add(MethodInvocation.invoke(Methods.matches2()));
				code.add(new IfEq(conditionFailsLabel));
			} else {
				// Call ConditionService.matches(Class)
				code.add(MethodVariableAccess.REFERENCE.loadFrom(3));
				code.add(ClassConstant.of((TypeDescription) annotatedElement));
				code.add(MethodInvocation.invoke(Methods.matches()));
				code.add(new IfEq(conditionFailsLabel));
			}
			return code;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}