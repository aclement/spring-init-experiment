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

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.support.GenericApplicationContext;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationValue;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Label;
import plugin.custom.ArrayLength;
import plugin.custom.IfNe;

/**
 * @author Andy Clement
 */
public class ConditionalOnMissingBeanHandler extends BaseConditionalHandler {

	public ConditionalOnMissingBeanHandler() {
		super(ConditionalOnMissingBean.class);
	}

	@Override
	public boolean accept(AnnotationDescription description) {
		return description.getAnnotationType().represents(ConditionalOnMissingBean.class);
	}

	@Override
	public Collection<? extends StackManipulation> computeStackManipulations(AnnotationDescription annoDescription,
			Object annotatedElement, Label conditionFailsLabel) {
		try {
			List<StackManipulation> code = new ArrayList<>();
			AnnotationValue<?, ?> value = annoDescription.getValue(valueProperty);
			// TODO don't ignore that value since sometimes don't want to use the
			// return type of the annotated method
			// TypeDescription[] classes = (TypeDescription[]) value.resolve();

			// What to call: if
			// (context.getBeanFactory().getBeanNamesForType(Gson.class).length == 0)
			code.add(MethodVariableAccess.REFERENCE.loadFrom(1)); // Load context
			code.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(
					GenericApplicationContext.class.getMethod("getBeanFactory"))));
			TypeDescription returnTypeOfBeanMethod = ((MethodDescription.InDefinedShape) annotatedElement)
					.getReturnType().asErasure();
			code.add(ClassConstant.of(returnTypeOfBeanMethod));
			code.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(
					ConfigurableListableBeanFactory.class.getMethod("getBeanNamesForType", Class.class))));
			code.add(new ArrayLength());
			code.add(new IfNe(conditionFailsLabel));
			return code;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}