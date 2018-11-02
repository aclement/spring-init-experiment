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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

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

	public static List<TypeElement> getTypesFromAnnotation(Types types, AnnotationMirror annotationMirror, String fieldname) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotationMirror.getElementValues();
		TypeCollector typeCollector = new TypeCollector(types);
		for (ExecutableElement element : values.keySet()) {
			if (element.getSimpleName().toString().equals(fieldname)) {
				values.get(element).accept(typeCollector, null);
			}
		}
		return typeCollector.collectedTypes();
	}

	// TODO [ac] isn't there a quicker way?
	private static class TypeCollector implements AnnotationValueVisitor<Boolean, Object> {

		private Types types;
		
		private List<TypeElement> collected = new ArrayList<>();
		
		List<TypeElement> collectedTypes() {
			return collected;
		}
		
		TypeCollector(Types types) {
			this.types = types;
		}
		
		@Override
		public Boolean visit(AnnotationValue av, Object p) {
			return av.accept(this, p);
		}

		@Override
		public Boolean visit(AnnotationValue av) {
			return av.accept(this, null);
		}

		@Override
		public Boolean visitBoolean(boolean b, Object p) {
			return false;
		}

		@Override
		public Boolean visitByte(byte b, Object p) {
			return false;
		}

		@Override
		public Boolean visitChar(char c, Object p) {
			return false;
		}

		@Override
		public Boolean visitDouble(double d, Object p) {
			return false;
		}

		@Override
		public Boolean visitFloat(float f, Object p) {
			return false;
		}

		@Override
		public Boolean visitInt(int i, Object p) {
			return false;
		}

		@Override
		public Boolean visitLong(long i, Object p) {
			return false;
		}

		@Override
		public Boolean visitShort(short s, Object p) {
			return false;
		}

		@Override
		public Boolean visitString(String s, Object p) {
			return false;
		}

		@Override
		public Boolean visitType(TypeMirror t, Object p) {
			Element e = types.asElement(t);
			if (e != null) {
				collected.add(((TypeElement)e));
			}
			return false;
		}

		@Override
		public Boolean visitEnumConstant(VariableElement c, Object p) {
			return false;
		}

		@Override
		public Boolean visitAnnotation(AnnotationMirror a, Object p) {
			return false;
		}

		@Override
		public Boolean visitArray(List<? extends AnnotationValue> vals, Object p) {
			for (AnnotationValue value : vals) {
				// TODO: really?
				if (this.visit(value) || value.toString().equals("<error>")) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Boolean visitUnknown(AnnotationValue av, Object p) {
			return false;
		}

	}

}
