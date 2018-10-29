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
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.collection.ArrayFactory;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Label;
import plugin.Methods;
import plugin.Types;
import plugin.custom.IfEq;

/**
 * @author Andy Clement
 */
public class ProfileConditionHandler extends BaseConditionalHandler {
	public ProfileConditionHandler() {
		super(Types.Profile());
	}

	@Override
	public boolean accept(AnnotationDescription description) {
		return description.getAnnotationType().equals(Types.Profile());
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
			code.add(MethodInvocation.invoke(Methods.getEnvironment()));
			code.add(ArrayFactory.forType(new TypeDescription.ForLoadedType(String.class).asGenericType())
					.withValues(profilesArrayEntries));
			code.add(MethodInvocation.invoke(Methods.of()));
			code.add(MethodInvocation.invoke(Methods.acceptsProfiles()));
			code.add(new IfEq(conditionFailsLabel));
			return code;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
