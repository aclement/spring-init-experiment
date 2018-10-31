package processor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

@SupportedAnnotationTypes({ "*" })
public class SlimConfigurationProcessor extends AbstractProcessor {

	private Types types;

	private Elements elements;

	private Filer filer;

	private Messager messager;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.types = processingEnv.getTypeUtils();
		this.elements = processingEnv.getElementUtils();
		this.filer = processingEnv.getFiler();
		this.messager = processingEnv.getMessager();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		process(ElementFilter.typesIn(roundEnv.getRootElements()));
		return true;
	}

	private void process(Set<TypeElement> types) {
		ModuleSpecs specs = new ModuleSpecs(this.types, this.elements);
		for (TypeElement type : types) {
			if (hasAnnotation(type, SpringClassNames.CONFIGURATION.toString())) {
				messager.printMessage(Kind.NOTE, "Found @Configuration", type);
				specs.addInitializer(type);
			}
			if (hasAnnotation(type,
					SpringClassNames.SPRING_BOOT_CONFIGURATION.toString())) {
				messager.printMessage(Kind.NOTE, "Found @SpringBootConfiguration", type);
				specs.addModule(type);
			}
		}
		for (ModuleSpec module : specs.getModules()) {
			module.process();
			for (InitializerSpec initializer : module.getInitializers()) {
				messager.printMessage(Kind.NOTE,
						"Writing Initializer " + initializer.getInitializer(),
						initializer.getConfigurationType());
				write(initializer.getInitializer(), initializer.getPackage());
			}
			messager.printMessage(Kind.NOTE, "Writing Module " + module.getModule(),
					module.getRootType());
			write(module.getModule(), module.getPackage());
		}
	}

	private boolean hasAnnotation(Element element, String type) {
		return getAnnotation(element, type) != null;
	}

	private AnnotationMirror getAnnotation(Element element, String type) {
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

	private void write(TypeSpec type, String packageName) {
		JavaFile file = JavaFile.builder(packageName, type).build();
		try {
			file.writeTo(this.filer);
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}