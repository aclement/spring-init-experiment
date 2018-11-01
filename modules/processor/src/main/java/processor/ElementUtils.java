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

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

/**
 * @author Dave Syer
 *
 */
public abstract class ElementUtils {

	public static boolean hasAnnotation(Element element, String type) {
		return getAnnotation(element, type) != null;
	}

	public static AnnotationMirror getAnnotation(Element element, String type) {
		return getAnnotation(element, type, new HashSet<>());
	}

	private static AnnotationMirror getAnnotation(Element element, String type,
			Set<AnnotationMirror> seen) {
		if (element != null) {
			for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
				if (annotation.getAnnotationType().toString().startsWith("java.lang")) {
					continue;
				}
				if (type.equals(annotation.getAnnotationType().toString())) {
					return annotation;
				}
				if (!seen.contains(annotation)) {
					seen.add(annotation);
					annotation = getAnnotation(annotation.getAnnotationType().asElement(),
							type, seen);
					if (annotation != null) {
						return annotation;
					}
				}
			}
		}
		return null;
	}

}
