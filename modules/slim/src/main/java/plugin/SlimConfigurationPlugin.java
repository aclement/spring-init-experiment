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
package plugin;

import static net.bytebuddy.matcher.ElementMatchers.named;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarFile;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ClassUtils;

import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.ClassFileLocator.ForFolder;
import net.bytebuddy.dynamic.ClassFileLocator.ForJarFile;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.jar.asm.Opcodes;
import plugin.internal.Type;
import plugin.internal.TypeSystem;
import slim.ImportModule;
import slim.Module;

public class SlimConfigurationPlugin implements Plugin {

	private InitializerClassFactory initializerClassFactory;

	private ModuleClassFactory moduleClassFactory;

	private TypeSystem ts;

	public SlimConfigurationPlugin() {
		initializerClassFactory = new InitializerClassFactory();
		moduleClassFactory = new ModuleClassFactory();
	}

	@Override
	public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassFileLocator locator) {
		try {
			// Set context classloader?

			File targetClassesFolder = locateTargetClasses(locator);
			URLClassLoader projectCl = setContextClassLoader(locator);
			ts = new TypeSystem(projectCl);
			if (targetClassesFolder == null) {
				log("Unable to determine target/classes folder for module");
				targetClassesFolder = new File("target/classes");
			}
			// TODO: skip this class if it was already processed
			DynamicType initializerClassType = initializerClassFactory.make(typeDescription, locator);
			initializerClassType.saveIn(targetClassesFolder);
			// TODO: fix this so it creates a module properly (and only when needed - one
			// per app)
			if (Common.hasAnnotation(typeDescription, SpringBootConfiguration.class)) {
				TypeDescription[] configs = findConfigs(typeDescription);
				log("Discovering @Import on " + typeDescription.getActualName() + ", found: " + toString(configs));
				// CRUDE - Create a subset of the configs where those that have module
				// references are not included (i.e. presume
				// the module reference will do the initialization stuff instead of
				// looking for a $$initializer in the config)
				List<TypeDescription> configSubset = new ArrayList<>();
				configSubset.add(typeDescription);
				for (TypeDescription config : configs) {
					// Name based - crude...
					boolean skip = false;
					System.out.println(config.getSimpleName());
					// Exclude spring library imports (they won't have the $$initializer
					// method)
					if (!Common.hasAnnotation(config, ImportModule.class) && config.getName().startsWith("org.springframework")) {
						skip = true;
					}
					if (!skip) {
						System.out.println("Not skipping " + config);
						configSubset.add(config);
					} else {
						System.out.println("Skipping " + config);
					}
				}
				DynamicType moduleClassType = moduleClassFactory.make(typeDescription, locator,
						configSubset.toArray(new TypeDescription[0]));
				builder = addSlimConfigurationAnnotation(builder, moduleClassType);
				log("Saving: " + moduleClassType.getTypeDescription() + " in " + targetClassesFolder);
				moduleClassType.saveIn(targetClassesFolder);

				// Here we go, testing module creation:
				createModuleIfReachable("org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration", locator,
						targetClassesFolder);
				createModuleIfReachable("org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration", locator,
						targetClassesFolder);
				createModuleIfReachable("org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration", locator,
						targetClassesFolder);
				createModuleIfReachable("org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerAutoConfiguration",
						locator, targetClassesFolder);
			}
			builder = Common.addInitializerMethod(builder, initializerClassType);

			return builder;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public void createModuleIfReachable(String autoConfigurationClassName, ClassFileLocator locator, File targetClassesFolder)
			throws Exception {
		if (ClassUtils.isPresent(autoConfigurationClassName, Thread.currentThread().getContextClassLoader())) {
			createModuleForAutoConfiguration(autoConfigurationClassName, locator, targetClassesFolder);
		} else {
			log(":debug: Unable to create module for " + autoConfigurationClassName + " not reachable here");
			return;
		}
	}

	/**
	 * For any specified auto-configuration this will create the module. For
	 * example, passing
	 * "org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration" will
	 * create
	 * "org.springframework.boot.autoconfigure.gson.GsonAutoConfigurationModule" and
	 * the initializer
	 * "org.springframework.boot.autoconfigure.gson.GsonAutoConfigurationModule$Initializer".
	 * 
	 */
	private void createModuleForAutoConfiguration(String autoConfigurationClass, ClassFileLocator locator, File targetFolder)
			throws Exception {
		log("\n\n\n:debug: creating module for " + autoConfigurationClass);
		String moduleName = autoConfigurationClass + "Module";
		String moduleInitializerName = moduleName + "$Initializer";

		updateSpringFactories(targetFolder, moduleName);

		List<TypeDescription> initializerTypes = new ArrayList<>();

		// Create the inner class initializer for the auto configuration (implements
		// ApplicationContextInitializer)
		DynamicType newModuleInitializerType = null;
		log(":debug: creating module initializer (the inner class) called " + moduleInitializerName);
		try {
			TypeDescription newModuleInitializer = new TypeDescription.Latent(moduleInitializerName, Opcodes.ACC_PUBLIC,
					TypeDescription.Generic.OBJECT);
			newModuleInitializerType = initializerClassFactory.make(
					new TypeDescription.ForLoadedType(
							ClassUtils.forName(autoConfigurationClass, Thread.currentThread().getContextClassLoader())),
					moduleInitializerName, locator);
			newModuleInitializerType.saveIn(targetFolder);
		} catch (Throwable t) {
			throw new IllegalStateException("Problem creating module initializer " + moduleInitializerName, t);
		}

		// Does the auto configuration class have any import references to other
		// configuration
		List<plugin.internal.Type> importedConfigurationTypes = findImports(ts.resolveDotted(autoConfigurationClass));
		if (importedConfigurationTypes.size() != 0) {
			log("Generating initializers for the @Imported references from configuration " + toShortName(autoConfigurationClass)
					+ ": " + importedConfigurationTypes);
			try {
				for (Type t : importedConfigurationTypes) {
					Class clazz = Class.forName(t.getName().replace("/", "."), false,
							Thread.currentThread().getContextClassLoader());
					DynamicType initializer = initializerClassFactory.make(new TypeDescription.ForLoadedType(clazz),
							moduleName + "$" + t.getShortName() + "_" + "Initializer", locator);
					initializer.saveIn(targetFolder);
					initializerTypes.add(initializer.getTypeDescription());
				}
			} catch (Throwable t) {
				throw new IllegalStateException("Problem creating module initializer inner class for imported configurations: "
						+ importedConfigurationTypes, t);
			}
		}

		try {
			log("Generating module code for " + moduleName);
			TypeDescription newModule = new TypeDescription.Latent(moduleName, Opcodes.ACC_PUBLIC, TypeDescription.Generic.OBJECT);
			DynamicType moduleClassType = moduleClassFactory.make(newModule, autoConfigurationClass, locator,
					newModuleInitializerType, null, initializerTypes.toArray(new TypeDescription[] {}));
			moduleClassType.saveIn(targetFolder);
		} catch (Throwable t) {
			throw new IllegalStateException("Unexpected problem generating code for module " + moduleName);
		}
	}

	private void updateSpringFactories(File targetFolder, String moduleName) {
		if (true) {
			// No point until we work out how to load these plus the slim.Module entries in
			// the slim module
			return;
		}
		File f = new File(targetFolder, "META-INF/spring.factories");
		if (f.exists()) {
			throw new IllegalStateException();
		}
		try {
			FileOutputStream fos = new FileOutputStream(f);
			DataOutputStream dos = new DataOutputStream(fos);
			dos.writeChars("slim.Module=" + moduleName);
			dos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
//		slim.Module=\
//				boot.autoconfigure.context.ContextAutoConfigurationModule,\
//				boot.autoconfigure.gson.GsonAutoConfigurationModule,\		
	}

	private String toString(TypeDescription[] tds) {
		StringBuilder s = new StringBuilder();
		s.append("[");
		if (tds != null) {
			for (int i = 0; i < tds.length; i++) {
				if (i > 0)
					s.append(",");
				s.append(tds[i].getName());
			}
		}
		s.append("]");
		return s.toString();
	}

	@SuppressWarnings("unchecked")
	private File locateTargetClasses(ClassFileLocator compoundLocator) {
		try {
			Field classFileLocatorsField = compoundLocator.getClass().getDeclaredField("classFileLocators");
			classFileLocatorsField.setAccessible(true);
			File found = null;
			List<ClassFileLocator> classFileLocators = (List<ClassFileLocator>) classFileLocatorsField.get(compoundLocator);
			for (ClassFileLocator classFileLocator : classFileLocators) {
				Field folderField = classFileLocator.getClass().getDeclaredField("folder");
				folderField.setAccessible(true);
				File ff = (File) folderField.get(classFileLocator);
				if (ff.toString().endsWith("target/classes")) {
					found = ff;
					break;
				}
			}
			return found;
		} catch (Exception e) {
			return null;
		}
	}

	private URLClassLoader setContextClassLoader(ClassFileLocator compoundLocator) {
		List<URL> classpathElements = new ArrayList<>();
		URLClassLoader ucl = null;
		try {
			Field classFileLocatorsField = compoundLocator.getClass().getDeclaredField("classFileLocators");
			classFileLocatorsField.setAccessible(true);
			File found = null;
			List<ClassFileLocator> classFileLocators = (List<ClassFileLocator>) classFileLocatorsField.get(compoundLocator);
			for (ClassFileLocator classFileLocator : classFileLocators) {
				if (classFileLocator instanceof ForFolder) {
					Field folderField = classFileLocator.getClass().getDeclaredField("folder");
					folderField.setAccessible(true);
					File ff = (File) folderField.get(classFileLocator);
//					System.out.println(">" + ff.toString());
					classpathElements.add(new File(ff.toString()).toURI().toURL());
					continue;
				}
				if (classFileLocator instanceof ForJarFile) {
					Field jarFileField = classFileLocator.getClass().getDeclaredField("jarFile");
					jarFileField.setAccessible(true);
					JarFile jarFile = (JarFile) jarFileField.get(classFileLocator);
//					System.out.println(">" + jarFile.getName());
					classpathElements.add(new File(jarFile.getName()).toURI().toURL());
					continue;
				}
				throw new IllegalStateException("WTF is this? " + classFileLocator.getClass().getName());
			}
			ClassLoader ccl = Thread.currentThread().getContextClassLoader();
			URL[] urls = classpathElements.toArray(new URL[0]);
//			System.out.println("Computed CP is ");
//			for (int i = 0; i < urls.length; i++) {
//				System.out.println(urls[i]);
//			}
			ucl = new ChildFirstURLClassLoader(urls, ccl);
			Thread.currentThread().setContextClassLoader(ucl);
			return ucl;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static class ChildFirstURLClassLoader extends URLClassLoader {

		public ChildFirstURLClassLoader(URL[] classpath, ClassLoader parent) {
			super(classpath, parent);
//	        system = getSystemClassLoader();
		}

		@Override
		protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			// First, check if the class has already been loaded
			Class<?> c = findLoadedClass(name);
			if (c == null) {
//	            if (system != null) {
//	                try {
//	                    // checking system: jvm classes, endorsed, cmd classpath, etc.
//	                    c = system.loadClass(name);
//	                }
//	                catch (ClassNotFoundException ignored) {
//	                }
//	            }
				if (c == null) {
					try {
						// checking local
						c = findClass(name);
					} catch (ClassNotFoundException e) {
						// checking parent
						// This call to loadClass may eventually call findClass again, in case the
						// parent doesn't find anything.
						c = super.loadClass(name, resolve);
					}
				}
			}
			if (resolve) {
				resolveClass(c);
			}
			return c;
		}

		@Override
		public URL getResource(String name) {
			URL url = null;
//	        if (system != null) {
//	            url = system.getResource(name); 
//	        }
			if (url == null) {
				url = findResource(name);
				if (url == null) {
					// This call to getResource may eventually call findResource again, in case the
					// parent doesn't find anything.
					url = super.getResource(name);
				}
			}
			return url;
		}

		@Override
		public Enumeration<URL> getResources(String name) throws IOException {
			/**
			 * Similar to super, but local resources are enumerated before parent resources
			 */
			Enumeration<URL> systemUrls = null;
//	        if (system != null) {
//	            systemUrls = system.getResources(name);
//	        }
			Enumeration<URL> localUrls = findResources(name);
			Enumeration<URL> parentUrls = null;
			if (getParent() != null) {
				parentUrls = getParent().getResources(name);
			}
			final List<URL> urls = new ArrayList<URL>();
			if (systemUrls != null) {
				while (systemUrls.hasMoreElements()) {
					urls.add(systemUrls.nextElement());
				}
			}
			if (localUrls != null) {
				while (localUrls.hasMoreElements()) {
					urls.add(localUrls.nextElement());
				}
			}
			if (parentUrls != null) {
				while (parentUrls.hasMoreElements()) {
					urls.add(parentUrls.nextElement());
				}
			}
			return new Enumeration<URL>() {
				Iterator<URL> iter = urls.iterator();

				public boolean hasMoreElements() {
					return iter.hasNext();
				}

				public URL nextElement() {
					return iter.next();
				}
			};
		}

		@Override
		public InputStream getResourceAsStream(String name) {
			URL url = getResource(name);
			try {
				return url != null ? url.openStream() : null;
			} catch (IOException e) {
			}
			return null;
		}

	}

	private TypeDescription[] findConfigs(TypeDescription typeDescription) {
		log("Finding imports for " + typeDescription);
		Collection<TypeDescription> result = new LinkedHashSet<>();
		for (AnnotationDescription imports : findImports(typeDescription)) {
			MethodList<MethodDescription.InDefinedShape> methodList = TypeDescription.ForLoadedType.of(Import.class)
					.getDeclaredMethods();
			InDefinedShape IMPORTS = methodList.filter(named("value")).getOnly();
			TypeDescription[] types = (TypeDescription[]) imports.getValue(IMPORTS).resolve();
			for (TypeDescription type : types) {
				if (!type.isAssignableTo(Module.class)) {
					log("Import " + type);
					result.add(type);
				}
			}
		}
		return result.toArray(new TypeDescription[0]);
	}


	private Predicate<plugin.internal.Annotation> annotationNamePredicate(Type annotationType) {
		return a -> {
			return a.isType(annotationType);
		};
	}

	private List<Type> findImports(Type t) {
		Type importAnnotation = ts.Lresolve("Lorg/springframework/context/annotation/Import;");
		List<plugin.internal.Annotation> importAnnotations = t.getDeclaredAnnotations(annotationNamePredicate(importAnnotation));
		System.out.println("Import annotations found on " + t.getName() + ": " + importAnnotations);
		if (importAnnotations.size() != 0) {
			return importAnnotations.get(0).getFieldListOfType("value");
		}
		return NO_TYPES;
	}

	private final static List<Type> NO_TYPES = Collections.emptyList();

	private List<AnnotationDescription> findImports(TypeDescription typeDescription) {
		List<AnnotationDescription> result = new ArrayList<>();
		for (AnnotationDescription candidate : typeDescription.getDeclaredAnnotations()) {
			AnnotationDescription found = Common.findMetaAnnotation(candidate, Import.class);
			if (found != null) {
				result.add(found);
			}
		}
		return result;
	}

	private Builder<?> addSlimConfigurationAnnotation(DynamicType.Builder<?> builder, DynamicType initializerClassType) {
		List<TypeDescription> initializers = new ArrayList<>();
		initializers.add(initializerClassType.getTypeDescription());
		return builder.annotateType(AnnotationDescription.Builder.ofType(ImportModule.class)
				.defineTypeArray("module", initializers.toArray(new TypeDescription[0])).build());
	}

	@Override
	public boolean matches(TypeDescription target) {
		log("Matching: " + target);
		return !Common.hasAnnotation(target, ImportModule.class) && Common.hasAnnotation(target, Configuration.class);
	}


	private void log(String message) {
		System.out.println(message);
	}

	private String toShortName(String dottedclassname) {
		int i = dottedclassname.lastIndexOf(".");
		return dottedclassname.substring(i + 1);
	}

}
