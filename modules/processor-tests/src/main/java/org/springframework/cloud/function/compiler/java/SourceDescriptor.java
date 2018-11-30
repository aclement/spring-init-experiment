package org.springframework.cloud.function.compiler.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Andy Clement
 *
 */
public class SourceDescriptor {

	private File file;
	private String classname;
	private String content;

	public SourceDescriptor(File file, String classname) {//, String content) {
		this.file = file;
		this.classname = classname;
//		this.content = content;
	}
	
	@Override
	public int hashCode() {
		return file.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof SourceDescriptor) && ((SourceDescriptor)obj).getFile().equals(file);
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

//
//	private static String getPackage(File f) { // a/b/C.java
//		String n = f.toString();
//		n =  n.substring(0,n.lastIndexOf("/")).replace("/",".");
//		return n;
//	}

	public String getPackageName() {
		int idx = classname.lastIndexOf(".");
		if (idx == -1) {
			return "";
		} else {
			return classname.substring(0,idx);
		}
	}
//
//	public String getName() {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
