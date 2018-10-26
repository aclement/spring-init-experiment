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

import org.springframework.context.annotation.Profile;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationValue;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.collection.ArrayFactory;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Label;
import plugin.custom.IfEq;

/**
 * @author Andy Clement
 */
public class ProfileConditionHandler extends BaseConditionalHandler {
	public ProfileConditionHandler() {
		super(Profile.class);
	}

	@Override
	public boolean accept(AnnotationDescription description) {
		return description.getAnnotationType().represents(Profile.class);
	}

	@Override
	public Collection<? extends StackManipulation> computeStackManipulations(AnnotationDescription annoDescription,
			Object annotatedElement, Label conditionFailsLabel) {
		try {
			// Invoke: context.getEnvironment().acceptsProfiles(Profiles.of((String[])
			// value))
			List<StackManipulation> code = new ArrayList<>();
			AnnotationValue<?, ?> value = annoDescription.getValue(valueProperty);
			// TODO I would prefer the unresolved references...
			String[] profiles = (String[]) value.resolve();
			List<StackManipulation> profilesArrayEntries = new ArrayList<>();
			for (int i = 0; i < profiles.length; i++) {
				profilesArrayEntries.add(new TextConstant(profiles[i]));
			}
			// ALOAD 1
			// INVOKEVIRTUAL
			// org/springframework/context/support/GenericApplicationContext.getEnvironment()Lorg/springframework/core/env/ConfigurableEnvironment;
			// ALOAD 5
			// CHECKCAST [Ljava/lang/String;
			// CHECKCAST [Ljava/lang/String;
			// INVOKESTATIC
			// org/springframework/core/env/Profiles.of([Ljava/lang/String;)Lorg/springframework/core/env/Profiles;
			// INVOKEINTERFACE
			// org/springframework/core/env/Environment.acceptsProfiles(Lorg/springframework/core/env/Profiles;)Z
			// IFEQ L7

			code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
			code.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(
					GenericApplicationContext.class.getMethod("getEnvironment"))));
			code.add(ArrayFactory.forType(new TypeDescription.ForLoadedType(String.class).asGenericType())
					.withValues(profilesArrayEntries));
			code.add(MethodInvocation.invoke(
					new MethodDescription.ForLoadedMethod(Profiles.class.getDeclaredMethod("of", String[].class))));
			code.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(
					Environment.class.getDeclaredMethod("acceptsProfiles", Profiles.class))));
			code.add(new IfEq(conditionFailsLabel));
			return code;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
