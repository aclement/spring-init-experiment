package processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

@SupportedAnnotationTypes({ "*" })
public class SlimConfigurationProcessor extends AbstractProcessor {

	private final static String SLIM_STATE_PATH = "META-INF/"
			+ "slim-configuration-processor.properties";

	private Filer filer;

	private Messager messager;

	private ModuleSpecs specs;

	private ElementUtils utils;

	private boolean processed;

	private Map<TypeElement, TypeElement> registrarInitializers = new HashMap<>();

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.filer = processingEnv.getFiler();
		this.messager = processingEnv.getMessager();
		this.utils = new ElementUtils(processingEnv.getTypeUtils(),
				processingEnv.getElementUtils(), this.messager);
		loadState();
		this.specs = new ModuleSpecs(this.utils, this.messager, this.filer, this.registrarInitializers);
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		// messager.printMessage(Kind.NOTE, "processor instance running #"+Integer.toHexString(System.identityHashCode(this)));
		if (roundEnv.processingOver()) {
			updateFactories();
			specs.saveModuleSpecs();
			saveState();
		}
		else if (!processed) {
			process(roundEnv);
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
			messager.printMessage(Kind.OTHER, "Cannot open spring.factories for reading");
		}
		String values = properties.getProperty(SpringClassNames.MODULE.toString());
		if (values == null) {
			values = "";
		}
		StringBuilder builder = new StringBuilder(values);
		for (ModuleSpec module : specs.getModules()) {
			if (module.getModule() != null && !values.contains(module.getClassName().toString())) {
				if (builder.length() > 0) {
					builder.append(",");
				}
				builder.append(module.getClassName().toString());
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

	private Set<TypeElement> collectTypes(RoundEnvironment roundEnv, Predicate<TypeElement> typeSelectionCondition) {
		Set<TypeElement> types = new HashSet<>();
		for (TypeElement type : ElementFilter.typesIn(roundEnv.getRootElements())) {
			collectTypes(type, types, typeSelectionCondition);
		}
		return types;
	}

	private void collectTypes(TypeElement type, Set<TypeElement> types, Predicate<TypeElement> typeSelectionCondition) {
		if (typeSelectionCondition.test(type)) {
			types.add(type);
			for (Element element : type.getEnclosedElements()) {
				if (element instanceof TypeElement) {
					collectTypes((TypeElement) element, types, typeSelectionCondition);
				}
			}
		}
	}
	
	private void process(RoundEnvironment roundEnv) {
		Set<TypeElement> types = collectTypes(roundEnv,
				te -> te.getKind() == ElementKind.CLASS
				&& !te.getModifiers().contains(Modifier.ABSTRACT)
				&& !te.getModifiers().contains(Modifier.STATIC));
		for (TypeElement type : types) {
			if (utils.hasAnnotation(type, SpringClassNames.CONFIGURATION.toString())) {
				messager.printMessage(Kind.NOTE, "Found @Configuration in " + type, type);
				specs.addInitializer(type);
			}
			if (utils.hasAnnotation(type,
					SpringClassNames.SPRING_BOOT_CONFIGURATION.toString())) {
				messager.printMessage(Kind.NOTE,
						"Found @SpringBootConfiguration in " + type, type);
				specs.addModule(type);
			}
			else if (utils.hasAnnotation(type, SpringClassNames.MODULE_ROOT.toString())) {
				messager.printMessage(Kind.NOTE, "Found @ModuleRoot in " + type, type);
				specs.addModule(type);
			}
		}
		discoverAndProcessAtEnabledRegistrarsAndSelectors(roundEnv);
		// Work out what these modules include
		for (ModuleSpec module: specs.getModules()) {
			module.prepare(specs);
		}
		for (ModuleSpec module : specs.getModules()) {
			module.produce(specs);
			for (InitializerSpec initializer : module.getInitializers()) {
				initializer.setModuleName(module.getClassName());
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

	private void discoverAndProcessAtEnabledRegistrarsAndSelectors(RoundEnvironment roundEnv) {
		Set<TypeElement> annotationTypes = collectTypes(roundEnv, te -> te.getKind() == ElementKind.ANNOTATION_TYPE /* TODO visibility check? */);
		for (TypeElement type: annotationTypes) {
			AnnotationMirror annotationMirror = utils.getAnnotation(type, SpringClassNames.IMPORT.toString());
			List<TypeElement> typesFromAnnotation = utils.getTypesFromAnnotation(annotationMirror, "value");
			for (TypeElement te: typesFromAnnotation) {
				if (utils.implementsInterface(te,SpringClassNames.IMPORT_BEAN_DEFINITION_REGISTRAR)) {
					registrarInitializers.put(type, te); // @EnableBar > SampleRegistrar
					System.out.println("Recording registrar @"+type+" > "+te);
				} else {
					// TODO support import selectors
//					// TODO For @EnableXX with import({Foo.class, Bar.class}) remember these mappings?
//					List<TypeElement> referencedConfiguration = atEnablers.get(type);
//					atEnablers.put(type, te);
				}
			}
		}
		// Create registrar initializers - same naming scheme as for configuration initializers
		// - could push this code into InitializerSpec (and have two - or more - kinds in there)
		for (Map.Entry<TypeElement, TypeElement> registrar: registrarInitializers.entrySet()) {
			ClassName initializerName = InitializerSpec.toInitializerNameFromConfigurationName(registrar.getKey());
			messager.printMessage(Kind.NOTE, "Creating registrar initializer class: "+initializerName);
			Builder builder = TypeSpec.classBuilder(initializerName);
			builder.addSuperinterface(SpringClassNames.INITIALIZER_TYPE);
			builder.addModifiers(Modifier.PUBLIC);
			MethodSpec.Builder mb = MethodSpec.methodBuilder("initialize");
			mb.addAnnotation(Override.class);
			mb.addModifiers(Modifier.PUBLIC);
			mb.addParameter(SpringClassNames.GENERIC_APPLICATION_CONTEXT, "context");
			// TODO use a service to register the registrar rather than calling registerBeanDefinitions right now (like conditionservice)
			mb.addStatement("$T registrar = new $T()", registrar.getValue(), registrar.getValue());
			// TODO invoke relevant Aware related methods
			mb.addStatement("registrar.registerBeanDefinitions(new $T($T.class),context)",
					SpringClassNames.STANDARD_ANNOTATION_METADATA,registrar.getValue());
			MethodSpec ms = mb.build();
			builder.addMethod(ms);
			TypeSpec ts = builder.build();
			write(ts, ClassName.get(registrar.getKey()).packageName());
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
	
	public void loadState() {
		Properties properties = new Properties();
		try {
			FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "",
					SLIM_STATE_PATH);
			try (InputStream stream = resource.openInputStream();) {
				properties.load(stream);
			}
			for (Map.Entry<Object, Object> property: properties.entrySet()) {
				String annotationType = (String)property.getKey(); // registrarinitializer.XXXX.YYY.ZZZ
				// TODO need to cope with types being removed across incremental builds
				registrarInitializers.put(utils.asTypeElement(annotationType.substring("registrarinitializer.".length()+1)), utils.asTypeElement(((String)property.getValue())));
			}
			messager.printMessage(Kind.NOTE, "Loaded "+properties.size()+" registrar definitions");
		}
		catch (IOException e) {
			messager.printMessage(Kind.NOTE, "Cannot load "+SLIM_STATE_PATH+" (normal on first full build)");
		}
	}
	
	// TODO merge moduleSpecs state into just one overall annotation processor state, rather than multiple files
	public void saveState() {
		Properties properties = new Properties();
		for (Map.Entry<TypeElement, TypeElement> registrarInitializer: registrarInitializers.entrySet()) {
			// e.g. @EnableBar > SampleRegistrar
			properties.setProperty("registrarinitializer."+registrarInitializer.getKey().getQualifiedName().toString(), 
					registrarInitializer.getValue().getQualifiedName().toString());
		}
		try {
			FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", SLIM_STATE_PATH);
			try (OutputStream stream = resource.openOutputStream();) {
				properties.store(stream, "Created by " + getClass().getName());
			}
		}
		catch (IOException e) {
			messager.printMessage(Kind.NOTE, "Cannot write "+SLIM_STATE_PATH);
		}
	}

}
