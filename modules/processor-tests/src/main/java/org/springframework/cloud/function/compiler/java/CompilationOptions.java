package org.springframework.cloud.function.compiler.java;

/**
 * Options that include those that apply to the call to javac but also influence the behaviour of the compiler harness (e.g. should
 * it load classes it compiles)
 * 
 * @author Andy Clement
 */
public class CompilationOptions {
	
	private boolean shouldLoadClasses;
	
	public CompilationOptions() {
		shouldLoadClasses= false;
	}

	public void setLoadClasses(boolean b) {
		shouldLoadClasses = b;
	}

	public boolean shouldLoadClasses() {
		return shouldLoadClasses;
	}

}
