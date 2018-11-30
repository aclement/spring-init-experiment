package org.springframework.slim.processor.infra;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.aether.graph.Dependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.function.compiler.java.CompilationResult;
import org.springframework.cloud.function.compiler.java.DependencyResolver;
import org.springframework.cloud.function.compiler.java.InMemoryJavaFileObject;
import org.springframework.cloud.function.compiler.java.SourceDescriptor;
import org.springframework.core.io.FileUrlResource;

public class Utils {

	private final static Logger logger = LoggerFactory.getLogger(Utils.class);
	
	public static Map<File, List<File>> resolvedDependenciesCache = new HashMap<>();

	public static Set<String> findImports(Path sourceFile) {
		Pattern pkg = Pattern.compile("^package.* ([.a-zA-Z]*).*;.*$");
		Pattern p = Pattern.compile("^import.* ([.a-zA-Z]*)\\.[a-zA-Z\\*]*;.*$");
		try {
			return Files.readAllLines(sourceFile).stream().flatMap(l -> {
				Matcher mat = pkg.matcher(l);
				Set<String> s = new HashSet<>();
				while (mat.find()) {
					s.add(mat.group(1));
				}
				mat = p.matcher(l);
				while (mat.find()) {
					s.add(mat.group(1));
				}
				return s.stream();
			}).collect(Collectors.toSet());
		} catch (IOException ioe) {
			throw new IllegalStateException("Problem discovering imports in "+sourceFile, ioe);
		}
	}

	/**
	 * Use maven to resolve the dependencies of the specified folder (expected to
	 * contain a pom), and then resolve them to either entries in the maven cache or
	 * as target/classes folders in other modules in this project build.
	 * 
	 * @param projectRootFolder the project to be resolved (should contain the
	 *                          pom.xml)
	 * @return a list of jars/folders - folders used for local
	 */
	public static List<File> resolveProjectDependencies(File projectRootFolder) {
		List<File> resolvedDependencies = resolvedDependenciesCache.get(projectRootFolder);
		if (resolvedDependencies == null) {
			DependencyResolver engine = DependencyResolver.instance();
			try {
				File f = new File(projectRootFolder, "pom.xml");
				List<Dependency> dependencies = engine.dependencies(new FileUrlResource(f.toURI().toURL()));
				resolvedDependencies = new ArrayList<>();
				for (Dependency dependency : dependencies) {
					File resolvedDependency = null;
					// Example: spring-init-experiment:tests-lib:jar:1.0-SNAPSHOT
					if (dependency.toString().startsWith("spring-init-experiment:")) {
						// Resolve locally
						StringTokenizer st = new StringTokenizer(dependency.toString(), ":");
						st.nextToken();
						resolvedDependency = new File(projectRootFolder, "../" + st.nextToken() + "/target/classes")
								.getCanonicalFile();
						if (!resolvedDependency.exists()) {
							System.out.println("Bad miss? " + resolvedDependency.getAbsolutePath().toString());
							resolvedDependency = null;
						}
					} else {
						resolvedDependency = engine.resolve(dependency);
					}
					resolvedDependencies.add(resolvedDependency);
				}
				logger.debug("Resolved #{} dependencies: {}", resolvedDependencies.size(), resolvedDependencies);
				resolvedDependenciesCache.put(projectRootFolder, Collections.unmodifiableList(resolvedDependencies));
			} catch (Throwable e) {
				throw new IllegalStateException("Unexpected problem resolving dependencies", e);
			}
		}
		return resolvedDependencies;
	}

	static class CompilationResultClassLoader extends URLClassLoader {

		private CompilationResult cr;
		File f;

		public CompilationResultClassLoader(List<File> dependencies, CompilationResult cr, ClassLoader parent) {
			super(toUrls(dependencies), parent);
			f = dependencies.get(0);
			this.cr = cr;
		}

		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			synchronized (getClassLoadingLock(name)) {
				// First, check if the class has already been loaded

				Class<?> c = findLoadedClass(name);
				if (c != null) {
					if (resolve) {
						resolveClass(c);
					}
					return c;
				}
				// Child first...
				try {
					Class<?> findClass = findClass(name);
					if (findClass != null) {
						if (resolve) {
							resolveClass(findClass);
						}
						return findClass;
					}
				} catch (ClassNotFoundException ncfe) {
//	        		ncfe.printStackTrace();
				}
//	            Class<?> c = findLoadedClass(name);
//	            if (c == null) {
//	            	if (cr != null) {
//	    				for (CompiledClassDefinition ccd: cr.getCcds()) {
//	    					if (ccd.getClassName().equals(name)) {
//	    						byte[] bs = ccd.getBytes();
//	    						try {
//	    							System.out.println("Defining class "+name);
//	    							c = defineClass(name, bs, 0, bs.length);
//	    				            if (resolve) {
//	    				                resolveClass(c);
//	    				            }
//	    							return c;
//	    						} catch (ClassFormatError cfe) {
//	    							cfe.printStackTrace();
//	    							break;
//	    						}
//	    					}
//	    				}
//	    			}

				return super.loadClass(name, resolve);

//	                long t0 = System.nanoTime();
//	                try {
//	                    if (parent != null) {
//	                        c = parent.loadClass(name, false);
//	                    } else {
//	                        c = findBootstrapClassOrNull(name);
//	                    }
//	                } catch (ClassNotFoundException e) {
//	                    // ClassNotFoundException thrown if class not found
//	                    // from the non-null parent class loader
//	                }
//
//	                if (c == null) {
//	                    // If still not found, then invoke findClass in order
//	                    // to find the class.
//	                    long t1 = System.nanoTime();
//	                    c = findClass(name);
//
//	                    // this is the defining class loader; record the stats
//	                    sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
//	                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
//	                    sun.misc.PerfCounter.getFindClasses().increment();
//	                }
//	            }
//	            if (resolve) {
//	                resolveClass(c);
//	            }
//	            return c;
			}
		}

		// name = java.lang.String
		protected java.lang.Class<?> findClass(String name) throws ClassNotFoundException {
//			System.out.println("Looking for " + name);
			if (cr != null) {
				for (InMemoryJavaFileObject ccd : cr.getCcds()) {
					if (ccd.getName().equals(name)) {
						byte[] bs = ccd.getBytes();
						try {
							System.out.println("Defining class " + name);
							Class<?> c = defineClass(name, bs, 0, bs.length);
							return c;
						} catch (ClassFormatError cfe) {
							cfe.printStackTrace();
							break;
						}
					}
				}
			}
			return super.findClass(name);
		};

		@Override
		public Enumeration<URL> findResources(String name) throws IOException {
//			System.out.println("Asked to find resources: " + name);
			Enumeration<URL> findResources = super.findResources(name);
//			 System.out.println("Found any?"+findResources.hasMoreElements());
//			 new URL()
			List<URL> ls = new ArrayList<>();
			if (!findResources.hasMoreElements() && cr != null) {
				for (InMemoryJavaFileObject ccd : cr.getCcds()) {
					if (ccd.getName().equals(name)) {
//							byte[] bs = ccd.getBytes();
						URL url = new URL("jar:file:/" + f.toString() + "!/" + ccd.getName());
						System.out.println(url);
//							try {
//								System.out.println("Defining class "+name);
//								Class<?> c = defineClass(name, bs, 0, bs.length);
//								return c;
//							} catch (ClassFormatError cfe) {
//								cfe.printStackTrace();
//								break;
//							}
					}
				}
			}
			return ls.size() == 0 ? findResources : Collections.enumeration(ls);
//			 return findResources;
		}

		public URL findResource(String name) {
			System.out.println("Asked to find resource " + name);
//			for (CompiledClassDefinition ccd: cr.getCcds()) {
////				if (ccd.getClassName().equals(name)) {
//					System.out.println(" - checking "+ccd.getClassName());
////				}
//			}
//			if (true) throw new IllegalStateException("!!"+name);
			return super.findResource(name);
		};

		private static URL[] toUrls(List<File> dependencies) {
			return dependencies.stream().map(f -> {
				try {
					return f.toURI().toURL();
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return null;
				}
			}).collect(Collectors.toList()).toArray(new URL[0]);
		}

	}

	public static ClassLoader getCompilationResultClassLoader(List<File> dependencies, CompilationResult result, ClassLoader parent) {
		return new CompilationResultClassLoader(dependencies, result,parent);
	}

	public static Map<File, List<SourceDescriptor>> filesCache = new HashMap<>();

	public static List<SourceDescriptor> getFiles(File rootFolder) {
		List<SourceDescriptor> sourcesInFolder = filesCache.get(rootFolder);
		if (sourcesInFolder == null) {
			final List<SourceDescriptor> collectedFiles = new ArrayList<>();
			try {
				Files.walkFileTree(rootFolder.toPath(), new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						collectedFiles.add(new SourceDescriptor(file.toFile(),
								guessClassName(rootFolder.toPath().relativize(file).toFile())));
						return super.visitFile(file, attrs);
					}

					private String guessClassName(File file) {
						String s = file.toString();
						if (s.endsWith(".java")) {
							return s.substring(0, s.length() - 5).replace("/", ".");
						} else {
							return null;
						}
					}
				});
			} catch (IOException e) {
				throw new IllegalStateException("Problems walking folder: " + rootFolder, e);
			}
			sourcesInFolder = Collections.unmodifiableList(collectedFiles);
			filesCache.put(rootFolder, sourcesInFolder);
		}
		return sourcesInFolder;
	}

}
