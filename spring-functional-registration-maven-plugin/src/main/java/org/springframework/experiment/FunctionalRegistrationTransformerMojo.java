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
package org.springframework.experiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

@Mojo(name = "transform", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class FunctionalRegistrationTransformerMojo extends AbstractMojo {

	@Parameter(required = true, defaultValue = "${project}")
	protected MavenProject project;

	@Parameter(defaultValue = "${project.build.outputDirectory}", property = "outputDir", required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${project.compileClasspathElements}", required = true, readonly = true)
	protected List<String> compileClasspathElements;

	public void execute() throws MojoExecutionException {
		if (outputDirectory.exists()) {
			transformClassesInFolder(outputDirectory);
		}
	}
	
	private void transformClassesInFolder(File dir) {
		File[] files = dir.listFiles();
		for (File file: files) {
			if (file.isDirectory()) {
				transformClassesInFolder(file);
			} else {
				if (file.getName().endsWith(".class")) {
					transformClassFile(file);
				}
			}
		}
	}

	private void transformClassFile(File file) {
		try {
			FileInputStream inputStream = new FileInputStream(file);
			byte[] newBytes = rewriteBytes(inputStream);
			inputStream.close();
			if (newBytes != null) {
				FileOutputStream outputStream = new FileOutputStream(file);
				outputStream.write(newBytes);
				outputStream.close();
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Unexpected problem rewriting application class", ioe);
		}
	}
	
	private byte[] rewriteBytes(InputStream inputStream) throws IOException {
		byte[] classBytes = Utils.loadBytesFromStream(inputStream);
		ClassDescriptor classDescriptor = ClassDescriptor.forClassBytes(classBytes);
		if (!classDescriptor.isConfigurationClass()) {
//			System.out.println("Not transforming "+classDescriptor.getName());
			return null;
		} else {
			System.out.println("Transforming "+classDescriptor.getName());
			ClassReader fileReader = new ClassReader(classBytes);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			FunctionalRegistrationClassAdapter frca = new FunctionalRegistrationClassAdapter(cw);
			fileReader.accept(frca, 0);
			return cw.toByteArray();
		}
	}
}
