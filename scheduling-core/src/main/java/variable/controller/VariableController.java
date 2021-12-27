package variable.controller;

import lombok.Getter;
import lombok.Setter;
import representation.Solution;
import variable.Variable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class VariableController {

	protected LocalDateTime now = LocalDateTime.now();

	public abstract List<Variable> setupVariables(Map<Object, Object> parameters, double k);

	public abstract void recalculateSolutionDetails(List<Solution> offspringSolutions);
}
