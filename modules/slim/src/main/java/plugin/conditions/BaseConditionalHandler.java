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

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

/**
 * @author Andy Clement
 */
public abstract class BaseConditionalHandler implements ConditionalHandler {
	protected MethodDescription.InDefinedShape valueProperty;

	public BaseConditionalHandler(TypeDescription  annotationConditionTypeDescription) {
		if (annotationConditionTypeDescription != null) {
			valueProperty = annotationConditionTypeDescription.getDeclaredMethods().filter(em -> em.getName().equals("value")).get(0);
		}
	}
}