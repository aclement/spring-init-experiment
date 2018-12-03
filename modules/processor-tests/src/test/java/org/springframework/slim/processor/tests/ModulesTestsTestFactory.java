package org.springframework.slim.processor.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.cloud.function.compiler.java.CompilationResult;
import org.springframework.cloud.function.compiler.java.FileDescriptor;
import org.springframework.slim.processor.infra.CompilerRunner;
import org.springframework.slim.processor.infra.Utils;

/**
 * Find the tests in the module/tests/src/test/java folder, pull together the related
 * sources and compile and run the test.
 * 
 * @author Andy Clement
 */
public class ModulesTestsTestFactory {

	private static File moduleTestsFolder = new File("../tests");

	@TestFactory
	Stream<DynamicTest> moduleTests() {
		return findExistingTests().stream()
				.map(f -> DynamicTest.dynamicTest(f.getClassName(), () -> runTest(f)));
	}

	/**
	 * @return files matching: module/tests/src/test/java/*Tests.java
	 */
	List<FileDescriptor> findExistingTests() {
		return Utils.getFiles(new File(moduleTestsFolder,"src/test/java")).stream()
				.filter(f -> f.getClassName().endsWith("Tests"))
				.collect(Collectors.toList());
	}

	private static void runTest(FileDescriptor testSourceFile) {
		Set<FileDescriptor> inputForCompiler = new HashSet<>();
		inferOtherSources(testSourceFile, inputForCompiler);
		Set<FileDescriptor> resources = new HashSet<>();
		resources.addAll(Utils.getFiles(new File(moduleTestsFolder, "src/main/resources")));
		resources.addAll(Utils.getFiles(new File(moduleTestsFolder, "src/test/resources")));
		List<File> dependencies = new ArrayList<>(resolveModuleTestsProjectDependencies());
		System.out.println("For test " + testSourceFile.getClassName());
		System.out.println(" - compiling #" + inputForCompiler.size() + " sources");
		System.out.println(" - resources #" + resources.size() + " files");
		System.out.println(" - dependencies #" + dependencies.size());
		CompilationResult result = CompilerRunner.run(inputForCompiler, resources, dependencies);
		dependencies.add(0, result.dumpToTemporaryJar());
		Utils.executeTests(result, dependencies, testSourceFile);
	}

	private static List<File> resolveModuleTestsProjectDependencies() {
		return Utils.resolveProjectDependencies(moduleTestsFolder);
	}

//	private static void executeTest(FileDescriptor testSourceFile, CompilationResult result,
//			List<File> dependencies) {
//		try {
//			ClassLoader cl = Utils.getCompilationResultClassLoader(dependencies, result, Thread.currentThread().getContextClassLoader().getParent());
//			Thread.currentThread().setContextClassLoader(cl);
//			Class<?> junitLauncher = ClassUtils.forName("org.junit.platform.console.ConsoleLauncher", cl);
//			// Call execute rather than main to avoid process exit
//			Method declaredMethod = junitLauncher.getDeclaredMethod("execute", PrintStream.class, PrintStream.class,
//					String[].class);
//			// Type of o is ConsoleLauncherExecutionResult
//			Object o = declaredMethod.invoke(null, System.out, System.err,
//					(Object) new String[] { "-c", testSourceFile.getClassName(), "--details", "none" });
//			Method getExitCode = ClassUtils.forName("org.junit.platform.console.ConsoleLauncherExecutionResult", cl).getDeclaredMethod("getExitCode");
//			Integer i = (Integer)getExitCode.invoke(o);
//			if (i != 0) {
//				throw new IllegalStateException("Test failed");
//			}
//		} catch (Exception e) {
//			throw new IllegalStateException("Failed", e);
//		}
//	}

	private static void inferOtherSources(FileDescriptor testclass, Set<FileDescriptor> collector) {
		Set<String> importedPackages = Utils.findImports(testclass.getFile().toPath());
		String thisPkg = testclass.getPackageName();
		collector.add(testclass);
		// Find other Java sources in those packages that we should include
		List<FileDescriptor> mainJavaSources = Utils.getFiles(new File(moduleTestsFolder,"src/main/java")).stream()
				.filter(f -> importedPackages.contains(f.getPackageName())).collect(Collectors.toList());
		for (FileDescriptor f : mainJavaSources) {
			if (collector.add(f) && !f.getPackageName().equals(thisPkg)) {
				inferOtherSources(f, collector);
			}
		}
		List<FileDescriptor> testJavaSources = Utils.getFiles(new File(moduleTestsFolder,"src/test/java")).stream()
				.filter(f -> importedPackages.contains(f.getPackageName())).collect(Collectors.toList());
		for (FileDescriptor f : testJavaSources) {
			if (collector.add(f) && !f.getPackageName().equals(thisPkg)) {
				inferOtherSources(f, collector);
			}
		}
	}

}