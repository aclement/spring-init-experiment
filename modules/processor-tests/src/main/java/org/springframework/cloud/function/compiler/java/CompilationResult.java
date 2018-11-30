/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.cloud.function.compiler.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipException;

import javax.tools.JavaFileObject.Kind;

/**
 * Holder for the results of compilation. If compilation was successful the set
 * of classes that resulted from compilation will be available. If compilation
 * was not successful the error messages should provide information about why.
 * Note that compilation may succeed and yet there will still be informational
 * or warning messages collected.
 * 
 * @author Andy Clement
 * @author Mark Fisher
 */
public class CompilationResult {

	private boolean successfulCompilation;

	List<CompilationMessage> compilationMessages = new ArrayList<>();

	List<Class<?>> compiledClasses = new ArrayList<>();

	private Map<String, byte[]> classBytes = new HashMap<>();

	private List<File> resolvedAdditionalDependencies = new ArrayList<>();

	private List<InMemoryJavaFileObject> ccds;

	public CompilationResult(boolean successfulCompilation) {
		this.successfulCompilation = successfulCompilation;
	}

	public void addClassBytes(String name, byte[] bytes) {
		this.classBytes.put(name, bytes);
	}

	public void setResolvedAdditionalDependencies(List<File> resolvedAdditionalDependencies) {
		this.resolvedAdditionalDependencies = resolvedAdditionalDependencies;
	}

	public List<File> getResolvedAdditionalDependencies() {
		return this.resolvedAdditionalDependencies;
	}

	public byte[] getClassBytes(String classname) {
		return this.classBytes.get(classname);
	}

	public boolean wasSuccessful() {
		return successfulCompilation;
	}

	public List<Class<?>> getCompiledClasses() {
		return compiledClasses;
	}

	public List<CompilationMessage> getCompilationMessages() {
		return Collections.unmodifiableList(compilationMessages);
	}

	public void recordCompilationMessage(CompilationMessage message) {
		this.compilationMessages.add(message);
	}

	public void recordCompilationMessages(List<CompilationMessage> messages) {
		this.compilationMessages.addAll(messages);
	}

	public void setCompiledClasses(List<Class<?>> compiledClasses) {
		this.compiledClasses = compiledClasses;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Compilation result: #classes=" + compiledClasses.size() + "  #messages=" + compilationMessages.size()
				+ "\n");
		s.append("Compiled classes:\n").append(compiledClasses).append("\n");
		s.append("Compilation messages:\n").append(compilationMessages).append("\n");
		return s.toString();
	}

	public void setCompiledClassDefinitions(List<InMemoryJavaFileObject> ccds2) {
		this.ccds = ccds2;
	}

	public List<InMemoryJavaFileObject> getCcds() {
		return ccds;
	}

	public File dumpToZip() {
		File f = null;
		try {
			f = File.createTempFile("aaa", ".jar");
			System.out.println("Writing generated output to " + f);
			f.delete();
			JarOutputStream jos = new JarOutputStream(new FileOutputStream(f));
			Set<String> packages = new HashSet<>();
			for (InMemoryJavaFileObject ccd : ccds) {
				// spring.factories written twice, first time no content... dig into that later
				if (ccd.getBytes()== null) continue;
				String entryName = ccd.getName().startsWith("/")?ccd.getName().substring(1):ccd.getName(); // remove leading slash
				if (ccd.getKind() == Kind.CLASS) {
//					entryName = entryName.replace(".", "/");
				}
				String pkg = entryName.substring(0, entryName.lastIndexOf("/") + 1);
				packages.add(pkg);
//				System.out.println("Writing out "+entryName);
				JarEntry ze = new JarEntry(entryName);
				jos.putNextEntry(ze);
				byte[] bs = ccd.getBytes();
				jos.write(bs, 0, bs.length);
				jos.closeEntry();
			}
			for (String pkg : packages) {
				JarEntry ze = new JarEntry(pkg);
				jos.putNextEntry(ze);
				jos.closeEntry();
			}
			jos.close();
			f.deleteOnExit();
		} catch (Throwable t) {
			throw new IllegalStateException("Problem storing compilation result",t);
		}
		return f;
	}

}
