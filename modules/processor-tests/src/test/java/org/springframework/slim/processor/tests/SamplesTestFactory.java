package org.springframework.slim.processor.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Find the tests in the samples/XXX folder, pull together the related
 * sources and compile and run the test.
 * 
 * @author Andy Clement
 */
public class SamplesTestFactory {

	private static File samplesFolder = new File("../../samples");

	@TestFactory
	Stream<DynamicTest> samples() {
		return Arrays.asList(samplesFolder.listFiles()).stream()
				.filter(f -> f.isDirectory() && new File(f,"pom.xml").exists()).
				map(f -> DynamicTest.dynamicTest(f.getName(), () -> runTest(f)));
	}
	
	private static void runTest(File sampleFolder) {
		 List<FileDescriptor> testFiles = Utils.getFiles(new File(sampleFolder,"src/test/java")).stream().filter(f -> f.getClassName().endsWith("Tests"))
			.collect(Collectors.toList());
//		 if (collect.size()!=1) {
//			 throw new IllegalStateException("Too many tests: "+collect);
//		 }
//		FileDescriptor testSourceFile = collect.get(0);
		Set<FileDescriptor> inputForCompiler = new HashSet<>();
		inputForCompiler.addAll(Utils.getFiles(new File(sampleFolder,"src/main/java")));
		inputForCompiler.addAll(Utils.getFiles(new File(sampleFolder,"src/test/java")));
		Set<FileDescriptor> resources = new HashSet<>();
		resources.addAll(Utils.getFiles(new File(sampleFolder,"src/main/resources")));
		resources.addAll(Utils.getFiles(new File(sampleFolder,"src/test/resources")));

		
		List<File> junitDeps =
				resolveSampleProjectDependencies(new File("."))
					.stream().filter(d -> d.toString().contains("junit")).collect(Collectors.toList());
		List<File> dependencies = new ArrayList<>(resolveSampleProjectDependencies(sampleFolder));
		dependencies.addAll(junitDeps);
		System.out.println("For sample " + sampleFolder);
		System.out.println(" - compiling #" + inputForCompiler.size() + " sources");
		System.out.println(" - resources #" + resources.size() + " files");
		System.out.println(" - dependencies #" + dependencies.size());
		System.out.println(dependencies);
		CompilationResult result = CompilerRunner.run(inputForCompiler, resources, dependencies);
		dependencies.add(0, result.dumpToTemporaryJar());
		Utils.executeTests(result, dependencies, testFiles.toArray(new FileDescriptor[] {}));
	}

	private static List<File> resolveSampleProjectDependencies(File sampleFolder) {
		return Utils.resolveProjectDependencies(sampleFolder);
	}

//	private static void inferOtherSources(File sampleFolder, FileDescriptor testclass, Set<FileDescriptor> collector) {
//		Set<String> importedPackages = Utils.findImports(testclass.getFile().toPath());
//		String thisPkg = testclass.getPackageName();
//		collector.add(testclass);
//		// Find other Java sources in those packages that we should include
//		List<FileDescriptor> mainJavaSources = Utils.getFiles(moduleTestsSrcMainJava).stream()
//				.filter(f -> importedPackages.contains(f.getPackageName())).collect(Collectors.toList());
//		for (FileDescriptor f : mainJavaSources) {
//			if (collector.add(f) && !f.getPackageName().equals(thisPkg)) {
//				inferOtherSources(f, collector);
//			}
//		}
//		List<FileDescriptor> testJavaSources = Utils.getFiles(moduleTestsSrcTestJava).stream()
//				.filter(f -> importedPackages.contains(f.getPackageName())).collect(Collectors.toList());
//		for (FileDescriptor f : testJavaSources) {
//			if (collector.add(f) && !f.getPackageName().equals(thisPkg)) {
//				inferOtherSources(f, collector);
//			}
//		}
//	}

}