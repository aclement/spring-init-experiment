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
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Label;
import plugin.Methods;
import plugin.Types;
import plugin.custom.IfEq;
import plugin.custom.IfNe;

/**
 * @author Andy Clement
 */
public class ConditionalOnPropertyHandler extends BaseConditionalHandler {
	protected MethodDescription.InDefinedShape prefixProperty;
	protected MethodDescription.InDefinedShape nameProperty;
	protected MethodDescription.InDefinedShape matchIfMissingProperty;
	protected MethodDescription.InDefinedShape havingValueProperty;

	public ConditionalOnPropertyHandler() {
		super(Types.ConditionalOnProperty());
		prefixProperty = Methods.findMethod(Types.ConditionalOnProperty(),"prefix");
		nameProperty = Methods.findMethod(Types.ConditionalOnProperty(),"name");
		matchIfMissingProperty = Methods.findMethod(Types.ConditionalOnProperty(),"matchIfMissing");
		havingValueProperty = Methods.findMethod(Types.ConditionalOnProperty(),"havingValue");
	}

	@Override
	public boolean accept(AnnotationDescription description) {
		if (!description.getAnnotationType().equals(Types.ConditionalOnProperty())) {
			return false;
		}
		AnnotationValue<?, ?> av = description.getValue(havingValueProperty);
		String havingValue = (String) av.resolve();
		if (havingValue.length() != 0) {
			System.out.println("Unable to optimize " + description + " because havingValue is set");
			return false;
		}
		return true;
	}

	/**
	 * What this does - looks at ConditionalOnProperty annotation for a value or
	 * name being set as one or more property names. It checks the prefix to see if
	 * that has been set. Using this information it creates a list of properties to
	 * check for. It cannot handle havingValue being set right now. It does
	 * understand matchIfMissing.
	 */
	@Override
	public Collection<? extends StackManipulation> computeStackManipulations(AnnotationDescription annoDescription,
			Object annotatedElement, Label conditionFailsLabel) {
		try {

			// Iterate over properties calling
			// PropertyResolver.containsProperty(Ljava/lang/String;)Z
			List<StackManipulation> code = new ArrayList<>();

			AnnotationValue<?, ?> mim = annoDescription.getValue(matchIfMissingProperty);
			boolean matchIfMissing = (Boolean) mim.resolve();
			AnnotationValue<?, ?> prefix = annoDescription.getValue(prefixProperty);
			String prefixString = (String) prefix.resolve();
			AnnotationValue<?, ?> value = annoDescription.getValue(valueProperty);
			Object resolvedValue = value.resolve();
			if (resolvedValue == null) {
				resolvedValue = annoDescription.getValue(nameProperty).resolve();
			}
			List<String> properties = new ArrayList<>();
			if (resolvedValue instanceof String) {
				properties.add((String) resolvedValue);
			} else {
				for (String propertyString : (String[]) resolvedValue) {
					String propertyToCheck = (prefixString != null && prefixString.length() != 0)
							? prefix + "." + propertyString
							: propertyString;
					properties.add(propertyToCheck);
				}
			}
			resolvedValue = annoDescription.getValue(nameProperty).resolve();
			if (resolvedValue instanceof String) {
				properties.add((String) resolvedValue);
			} else {
				for (String propertyString : (String[]) resolvedValue) {
					String propertyToCheck = (prefixString != null && prefixString.length() != 0)
							? prefix + "." + propertyString
							: propertyString;
					properties.add(propertyToCheck);
				}
			}

			if (properties.size() != 0) {
				System.out.println("Processing ConditionalOnProperty found on " + annotatedElement);
			}

			code.add(MethodVariableAccess.REFERENCE.loadFrom(1));
			code.add(MethodInvocation.invoke(Methods.getEnvironment()));
			code.add(MethodVariableAccess.REFERENCE.storeAt(4));

			for (String propertyString : properties) {
				System.out.println("inserting " + (matchIfMissing ? "negative " : "") + "check for property '"
						+ propertyString + "'");
				code.add(MethodVariableAccess.REFERENCE.loadFrom(4));
				code.add(new TextConstant(propertyString));
				code.add(MethodInvocation.invoke(Methods.containsProperty()
						));
				if (matchIfMissing) {
					code.add(new IfNe(conditionFailsLabel));
				} else {
					code.add(new IfEq(conditionFailsLabel));
				}
			}
			return code;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}