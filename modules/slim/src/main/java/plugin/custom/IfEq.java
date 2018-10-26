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
package plugin.custom;

import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.implementation.Implementation.Context;

/**
 * @author Andy Clement
 */
public class IfEq implements StackManipulation {
	private final Label label;

	public IfEq(Label label) {
		this.label = label;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public Size apply(MethodVisitor methodVisitor, Context implementationContext) {
		methodVisitor.visitJumpInsn(Opcodes.IFEQ, label);
		return new StackManipulation.Size(-1, 0);
	}
}