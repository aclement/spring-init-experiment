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
		Set<TypeElement> types = collectTypes(roundEnv);
		process(types);
		return true;
	}

	private Set<TypeElement> collectTypes(RoundEnvironment roundEnv) {
		Set<TypeElement> types = new HashSet<>(
				ElementFilter.typesIn(roundEnv.getRootElements()));
		for (TypeElement type : new HashSet<>(types)) {
			collectTypes(type, types);
		}
		return types;
	}

	private void collectTypes(TypeElement type, Set<TypeElement> types) {
		for (Element element : type.getEnclosedElements()) {
			if (element instanceof TypeElement) {
				types.add((TypeElement) element);
				collectTypes((TypeElement) element, types);
			}
		}
	}

	private void process(Set<TypeElement> types) {
		ModuleSpecs specs = new ModuleSpecs(this.types, this.elements);
		for (TypeElement type : types) {
			if (ElementUtils.hasAnnotation(type,
					SpringClassNames.CONFIGURATION.toString())) {
				messager.printMessage(Kind.NOTE, "Found @Configuration", type);
				specs.addInitializer(type);
			}
			if (ElementUtils.hasAnnotation(type,
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
