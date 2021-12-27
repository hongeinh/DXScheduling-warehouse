package variable.controller.impl;

import representation.Solution;
import variable.Variable;
import variable.component.resource.Resource;

import java.util.List;
import java.util.Map;

public class FlexibleMultiorderVariableController extends FixedMultiorderVariableController {

	@Override
	protected List<Variable> assignResources(List<Variable> orders, Map<String, List<? extends Resource>> resources, double k) {
		return orders;
	}
}
