package org.springframework.slim.processor.tests;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.cloud.function.compiler.java.CompilationResult;
import org.springframework.cloud.function.compiler.java.SourceDescriptor;
import org.springframework.slim.processor.infra.CompilerRunner;
import org.springframework.slim.processor.infra.Utils;
import org.springframework.util.ClassUtils;

/**
 * Find the tests in the module/tests/src/test/java folder, pull together the
 * required sources and compile and run them.
 * 
 * @author Andy Clement
 */
public class ModulesTestsTestFactory {

	private static File moduleTestsFolder = new File("../tests");

	private static File moduleTestsSrcMainJava = new File(moduleTestsFolder, "src/main/java");

	private static File moduleTestsSrcTestJava = new File(moduleTestsFolder, "src/test/java");

	@TestFactory
	Stream<DynamicTest> spring_tests() {
		return findExistingTests().stream().map(f -> DynamicTest.dynamicTest(f.getClassName(), () -> runTest(f)));
	}

	/**
	 * @return files matching: module/tests/src/test/java/*Tests.java
	 */
	List<SourceDescriptor> findExistingTests() {
		return Utils.getFiles(moduleTestsSrcTestJava).stream().filter(f -> f.getClassName().endsWith("Tests"))
				.collect(Collectors.toList());
	}

	private static void runTest(SourceDescriptor testSourceFile) {
		Set<SourceDescriptor> inputForCompiler = new HashSet<>();
		inferOtherSources(testSourceFile, inputForCompiler);
		List<File> dependencies = new ArrayList<>(resolveModuleTestsProjectDependencies());
		System.out.println("For test " + testSourceFile.getClassName());
		System.out.println(" - compiling #" + inputForCompiler.size() + " files");
		System.out.println(" - dependencies #" + dependencies.size());
		CompilationResult result = CompilerRunner.run(inputForCompiler, dependencies);
		dependencies.add(0, result.dumpToZip());
		executeTest(testSourceFile, result, dependencies);
	}

	private static List<File> resolveModuleTestsProjectDependencies() {
		return Utils.resolveProjectDependencies(moduleTestsFolder);
	}

	private static void executeTest(SourceDescriptor testSourceFile, CompilationResult result,
			List<File> dependencies) {
		try {
			ClassLoader cl = Utils.getCompilationResultClassLoader(dependencies, result, Thread.currentThread().getContextClassLoader().getParent());
			Thread.currentThread().setContextClassLoader(cl);
			Class<?> junitLauncher = ClassUtils.forName("org.junit.platform.console.ConsoleLauncher", cl);
			// Call execute rather than main to avoid process exit
			Method declaredMethod = junitLauncher.getDeclaredMethod("execute", PrintStream.class, PrintStream.class,
					String[].class);
			// Type of o is ConsoleLauncherExecutionResult
			Object o = declaredMethod.invoke(null, System.out, System.err,
					(Object) new String[] { "-c", testSourceFile.getClassName(), "--details", "none" });
			Method getExitCode = ClassUtils.forName("org.junit.platform.console.ConsoleLauncherExecutionResult", cl).getDeclaredMethod("getExitCode");
			Integer i = (Integer)getExitCode.invoke(o);
			if (i != 0) {
				throw new IllegalStateException("Test failed");
			}
		} catch (Exception e) {
			throw new IllegalStateException("Failed", e);
		}
	}

	private static void inferOtherSources(SourceDescriptor testclass, Set<SourceDescriptor> collector) {
		Set<String> importedPackages = Utils.findImports(testclass.getFile().toPath());
		String thisPkg = testclass.getPackageName();
		collector.add(testclass);
		// Find other Java sources in those packages that we should include
		List<SourceDescriptor> mainJavaSources = Utils.getFiles(moduleTestsSrcMainJava).stream()
				.filter(f -> importedPackages.contains(f.getPackageName())).collect(Collectors.toList());
		for (SourceDescriptor f : mainJavaSources) {
			if (collector.add(f) && !f.getPackageName().equals(thisPkg)) {
				inferOtherSources(f, collector);
			}
		}
		List<SourceDescriptor> testJavaSources = Utils.getFiles(moduleTestsSrcTestJava).stream()
				.filter(f -> importedPackages.contains(f.getPackageName())).collect(Collectors.toList());
		for (SourceDescriptor f : testJavaSources) {
			if (collector.add(f) && !f.getPackageName().equals(thisPkg)) {
				inferOtherSources(f, collector);
			}
		}
	}

}