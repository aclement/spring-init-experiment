package processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Properties;
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
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

@SupportedAnnotationTypes({ "*" })
public class SlimConfigurationProcessor extends AbstractProcessor {

	private Filer filer;

	private Messager messager;

	private ModuleSpecs specs;

	private boolean processed;

	private ElementUtils utils;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.filer = processingEnv.getFiler();
		this.messager = processingEnv.getMessager();
		this.utils = new ElementUtils(processingEnv.getTypeUtils(),
				processingEnv.getElementUtils());
		this.specs = new ModuleSpecs(this.utils);
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			updateFactories();
		}
		else if (!processed) {
			Set<TypeElement> types = collectTypes(roundEnv);
			process(types);
			processed = true;
		}
		return true;
	}

	private void updateFactories() {
		Properties properties = new Properties();
		try {
			FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "",
					"META-INF/spring.factories");
			try (InputStream stream = resource.openInputStream()) {
				properties.load(stream);
			}
		}
		catch (IOException e) {
			messager.printMessage(Kind.NOTE, "Cannot open spring.factories for reading");
		}
		String values = properties.getProperty(SpringClassNames.MODULE.toString());
		if (values == null) {
			values = "";
		}
		StringBuilder builder = new StringBuilder(values);
		for (ModuleSpec module : specs.getModules()) {
			if (!values.contains(module.getClassName())) {
				if (builder.length() > 0) {
					builder.append(",");
				}
				builder.append(module.getClassName());
			}
		}
		properties.setProperty(SpringClassNames.MODULE.toString(), builder.toString());
		try {
			FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "",
					"META-INF/spring.factories");
			try (OutputStream stream = resource.openOutputStream();) {
				properties.store(stream, "Created by " + getClass().getName());
			}
		}
		catch (IOException e) {
			messager.printMessage(Kind.NOTE, "Cannot open spring.factories for writing");
		}
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
		for (TypeElement type : types) {
			if (utils.hasAnnotation(type, SpringClassNames.CONFIGURATION.toString())) {
				messager.printMessage(Kind.NOTE, "Found @Configuration", type);
				specs.addInitializer(type);
			}
			if (utils.hasAnnotation(type,
					SpringClassNames.SPRING_BOOT_CONFIGURATION.toString())) {
				messager.printMessage(Kind.NOTE, "Found @SpringBootConfiguration", type);
				specs.addModule(type);
			}
		}
		for (ModuleSpec module : specs.getModules()) {
			module.process();
			for (InitializerSpec initializer : module.getInitializers()) {
				messager.printMessage(Kind.NOTE,
						"Writing Initializer " + ClassName.get(initializer.getPackage(),
								initializer.getInitializer().name),
						initializer.getConfigurationType());
				write(initializer.getInitializer(), initializer.getPackage());
			}
			messager.printMessage(Kind.NOTE, "Writing Module " + module.getClassName(),
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
