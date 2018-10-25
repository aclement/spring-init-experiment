package plugin.internal;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.AnnotationNode;

public class Annotation {

	private TypeSystem ts;
	private AnnotationNode an;
	
	public Annotation(TypeSystem ts, AnnotationNode an) {
		this.ts = ts;
		this.an = an;
	}

	public String getName() {
		return an.desc;
	}
	
	public String toString() {
		return an.desc;
	}

	public boolean isType(Type annotationType) {
		// TODO improve that L handling...
		return getName().equals("L"+annotationType.getName()+";");
	}

	public List<Type> getFieldListOfType(String fieldName) {
		List<Object> values = an.values;
		for (int i=0, len=values.size();i<len;i+=2) {
			if (((String)values.get(i)).equals(fieldName)) {
				List<org.objectweb.asm.Type> types = (List<org.objectweb.asm.Type>)values.get(i+1);
				List<Type> result = new ArrayList<>();
				for (org.objectweb.asm.Type t: types) {
					result.add(ts.resolve(t));
				}
				return result;
			}
		}
		return null;
	}

}
