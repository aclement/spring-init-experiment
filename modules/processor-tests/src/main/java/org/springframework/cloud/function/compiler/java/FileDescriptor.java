package org.springframework.cloud.function.compiler.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Andy Clement
 *
 */
public class FileDescriptor {

	private File file;
	private String name;
	private String classname;
	private String content;

	public FileDescriptor(File file, String name, String classname) {
		this.file = file;
		this.name=name;
		this.classname = classname;
	}
	
	@Override
	public int hashCode() {
		return file.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof FileDescriptor) && ((FileDescriptor)obj).getFile().equals(file);
	}
	
	public String getClassName() {
		return classname;
	}

	public String getContent() {
		if (content == null) {
			try {
				content = new String(Files.readAllBytes(file.toPath()));
			} catch (IOException e) {
				throw new IllegalStateException("Failed to load "+file, e);
			}
		}
		return content;
	}
	
	public String toString() {
		return "Source["+file.toString()+"]";
	}

	public File getFile() {
		return file;
	}

	public String getPackageName() {
		int idx = classname.lastIndexOf(".");
		if (idx == -1) {
			return "";
		} else {
			return classname.substring(0,idx);
		}
	}

	public String getName() {
		return this.name;
	}

}
