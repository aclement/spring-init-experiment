package org.springframework.slim.processor.infra;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.springframework.cloud.function.compiler.java.CompilationMessage;
import org.springframework.cloud.function.compiler.java.CompilationOptions;
import org.springframework.cloud.function.compiler.java.CompilationResult;
import org.springframework.cloud.function.compiler.java.InMemoryJavaFileObject;
import org.springframework.cloud.function.compiler.java.RuntimeJavaCompiler;
import org.springframework.cloud.function.compiler.java.FileDescriptor;

public class CompilerRunner {

	public static CompilationResult run(Collection<FileDescriptor> sources, Collection<FileDescriptor> resources, List<File> dependencies) {
		RuntimeJavaCompiler compiler = new RuntimeJavaCompiler();
		CompilationOptions options = new CompilationOptions();
		boolean hasErrors = false;
		System.out.println("Starting compiler...");
		CompilationResult result = compiler.compile(sources.toArray(new FileDescriptor[0]),resources.toArray(new FileDescriptor[0]), options, dependencies);
		List<CompilationMessage> compilationMessages = result.getCompilationMessages();
		for (CompilationMessage compilationMessage : compilationMessages) {
			if (compilationMessage.getKind().toString().equals("OTHER")) {
				System.out.println(">> " + compilationMessage.getMessage());
			} else {
				if (compilationMessage.getKind().toString().equals("ERROR")) {
					hasErrors = true;
				}
				System.out.println(compilationMessage.getKind()+": "+compilationMessage.getMessage());
			}
		}
		if (hasErrors) {
			throw new IllegalStateException("Compilation failed, see errors");
		} else {
			System.out.println("Compilation completed OK");
		}
		//		System.out.println(result.getCompilationMessages().stream().map(cm -> cm.getKind() + "." + cm.getMessage())
		//				.collect(Collectors.toList()));
		// printCompilationSummary(result);
		return result;
	}

	private static void printCompilationSummary(CompilationResult cr) {
		List<InMemoryJavaFileObject> compiledClasses = cr.getCcds();
		if (compiledClasses == null) {
			System.out.println("NO CLASSES COMPILED");
			return;
		}
		System.out.println("Output files:");
		for (InMemoryJavaFileObject compiledClassDefinition : compiledClasses) {
			byte[] bytes = compiledClassDefinition.getBytes();
			System.out.println(compiledClassDefinition.getName() + " size:" + (bytes == null ? "NULL" : bytes.length));
		}
	}
}
