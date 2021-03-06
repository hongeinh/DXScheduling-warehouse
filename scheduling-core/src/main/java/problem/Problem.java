package problem;

import variable.controller.VariableController;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import representation.Solution;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public abstract class Problem {

	protected Map<Object, Object> parameters;
	protected VariableController variableController;

	public abstract Solution evaluate(Solution solution);

	public abstract double[] evaluateConstraints(Solution solution);

}
