package variable;

import java.io.Serializable;

public interface Variable extends Comparable<Variable>, Serializable {
	Object getValue();
	void setValue(Object value);
}
