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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author Dave Syer
 *
 */
public class ElementUtils {

	private Types types;
	private TypeCollector typeCollector = new TypeCollector();
	private TypeFinder typeFinder = new TypeFinder();
	private Elements elements;

	public ElementUtils(Types types, Elements elements) {
		this.types = types;
		this.elements = elements;
	}

	public boolean hasAnnotation(Element element, String type) {
		return getAnnotation(element, type) != null;
	}

	public AnnotationMirror getAnnotation(Element element, String type) {
		return getAnnotation(element, type, new HashSet<>());
	}

	private AnnotationMirror getAnnotation(Element element, String type,
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

	public TypeElement getSuperType(TypeElement type) {
		TypeMirror superType = type.getSuperclass();
		return (superType == null ? null : (TypeElement) types.asElement(superType));
	}

	public TypeMirror getReturnType(ExecutableElement method) {
		TypeMirror type = method.getReturnType();
		if (types.asElement(type).getModifiers().contains(Modifier.PRIVATE)) {
			// Hack, hack, hackety, hack...
			for (TypeMirror subtype : types.directSupertypes(type)) {
				Element element = types.asElement(subtype);
				// Find an interface, any interface...
				if (element.getModifiers().contains(Modifier.PUBLIC)
						&& element.getKind() == ElementKind.INTERFACE) {
					return subtype;
				}
			}
		}
		return type;
	}

	public String getQualifiedName(TypeElement type) {
		return types.erasure(type.asType()).toString();
	}

	public boolean findTypeInAnnotation(AnnotationMirror imported, String string,
			String className) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = imported
				.getElementValues();
		for (ExecutableElement element : values.keySet()) {
			if (values.get(element).accept(typeFinder, null)) {
				return true;
			}
		}
		return false;
	}

	public List<TypeElement> getTypesFromAnnotation(AnnotationMirror annotationMirror,
			String fieldname) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotationMirror
				.getElementValues();
		List<TypeElement> collected = new ArrayList<>();
		for (ExecutableElement element : values.keySet()) {
			if (element.getSimpleName().toString().equals(fieldname)) {
				values.get(element).accept(typeCollector, collected);
			}
		}
		return collected;
	}

	// TODO [ac] isn't there a quicker way?
	private class TypeCollector
			implements AnnotationValueVisitor<Boolean, List<TypeElement>> {

		@Override
		public Boolean visit(AnnotationValue av, List<TypeElement> collected) {
			return av.accept(this, collected);
		}

		@Override
		public Boolean visit(AnnotationValue av) {
			return av.accept(this, null);
		}

		@Override
		public Boolean visitBoolean(boolean b, List<TypeElement> collected) {
			return false;
		}

		@Override
		public Boolean visitByte(byte b, List<TypeElement> collected) {
			return false;
		}

		@Override
		public Boolean visitChar(char c, List<TypeElement> collected) {
			return false;
		}

		@Override
		public Boolean visitDouble(double d, List<TypeElement> collected) {
			return false;
		}

		@Override
		public Boolean visitFloat(float f, List<TypeElement> collected) {
			return false;
		}

		@Override
		public Boolean visitInt(int i, List<TypeElement> collected) {
			return false;
		}

		@Override
		public Boolean visitLong(long i, List<TypeElement> collected) {
			return false;
		}

		@Override
		public Boolean visitShort(short s, List<TypeElement> collected) {
			return false;
		}

		@Override
		public Boolean visitString(String s, List<TypeElement> collected) {
			return false;
		}

		@Override
		public Boolean visitType(TypeMirror t, List<TypeElement> collected) {
			Element e = types.asElement(t);
			if (e != null) {
				collected.add(((TypeElement) e));
			}
			return false;
		}

		@Override
		public Boolean visitEnumConstant(VariableElement c, List<TypeElement> collected) {
			return false;
		}

		@Override
		public Boolean visitAnnotation(AnnotationMirror a, List<TypeElement> collected) {
			return false;
		}

		@Override
		public Boolean visitArray(List<? extends AnnotationValue> vals,
				List<TypeElement> collected) {
			for (AnnotationValue value : vals) {
				// TODO: really?
				if (this.visit(value, collected) || value.toString().equals("<error>")) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Boolean visitUnknown(AnnotationValue av, List<TypeElement> collected) {
			return false;
		}

	}

	private class TypeFinder implements AnnotationValueVisitor<Boolean, String> {

		@Override
		public Boolean visit(AnnotationValue av, String name) {
			return av.accept(this, name);
		}

		@Override
		public Boolean visit(AnnotationValue av) {
			return av.accept(this, null);
		}

		@Override
		public Boolean visitBoolean(boolean b, String name) {
			return false;
		}

		@Override
		public Boolean visitByte(byte b, String name) {
			return false;
		}

		@Override
		public Boolean visitChar(char c, String name) {
			return false;
		}

		@Override
		public Boolean visitDouble(double d, String name) {
			return false;
		}

		@Override
		public Boolean visitFloat(float f, String name) {
			return false;
		}

		@Override
		public Boolean visitInt(int i, String name) {
			return false;
		}

		@Override
		public Boolean visitLong(long i, String name) {
			return false;
		}

		@Override
		public Boolean visitShort(short s, String name) {
			return false;
		}

		@Override
		public Boolean visitString(String s, String name) {
			return false;
		}

		@Override
		public Boolean visitType(TypeMirror t, String name) {
			if (types.asElement(t) == null
					|| ((TypeElement) types.asElement(t)).getQualifiedName() == null) {
				return false;
			}
			return ((TypeElement) types.asElement(t)).getQualifiedName().toString()
					.equals(name);
		}

		@Override
		public Boolean visitEnumConstant(VariableElement c, String name) {
			return false;
		}

		@Override
		public Boolean visitAnnotation(AnnotationMirror a, String name) {
			return false;
		}

		@Override
		public Boolean visitArray(List<? extends AnnotationValue> vals, String name) {
			for (AnnotationValue value : vals) {
				// TODO: really?
				if (this.visit(value, name) || value.toString().equals("<error>")) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Boolean visitUnknown(AnnotationValue av, String name) {
			return false;
		}

	}

	public boolean throwsCheckedException(ExecutableElement beanMethod) {
		TypeMirror exceptionType = elements.getTypeElement(Exception.class.getName())
				.asType();
		TypeMirror runtimeExceptionType = elements
				.getTypeElement(RuntimeException.class.getName()).asType();
		for (TypeMirror type : beanMethod.getThrownTypes()) {
			if (types.isSubtype(type, exceptionType)
					&& !types.isSubtype(type, runtimeExceptionType)) {
				return true;
			}
		}
		return false;
	}

	public TypeMirror erasure(Element element) {
		return types.erasure(element.asType());
	}

	public String getParameterType(VariableElement param) {
		return param.asType().toString();
	}

	public TypeMirror erasure(TypeMirror type) {
		if (type.getKind() == TypeKind.ARRAY) {
			// Erase the component type?
			return type;
		}
		Element element = types.asElement(type);
		if (element != null) {
			return erasure(element);
		}
		return type;
	}

}