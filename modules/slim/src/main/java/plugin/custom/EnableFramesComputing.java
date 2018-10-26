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

import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.pool.TypePool;

/**
 * @author Andy Clement
 */
public class EnableFramesComputing implements AsmVisitorWrapper {
	@Override
	public final int mergeWriter(int flags) {
		return flags | ClassWriter.COMPUTE_FRAMES;
	}

	@Override
	public final int mergeReader(int flags) {
		return flags | ClassWriter.COMPUTE_FRAMES;
	}

	@Override
	public final ClassVisitor wrap(TypeDescription td, ClassVisitor cv, Implementation.Context ctx, TypePool tp,
			FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods, int wflags, int rflags) {
		return cv;
	}
}
