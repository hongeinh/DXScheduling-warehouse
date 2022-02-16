package variable.controller.impl;

import representation.Solution;
import utils.DataUtil;
import variable.Variable;
import variable.component.resource.Resource;
import variable.component.resource.impl.HumanResource;
import variable.component.resource.impl.MachineResource;
import variable.component.skill.Skill;
import variable.controller.VariableController;
import variable.impl.Task;

import java.util.*;

public class FixedMultiorderVariableController extends VariableController {

	@Override
	public List<Variable> setupVariables(Map<Object, Object> parameters, double k) {
		List<Variable> orders = this.setupOrders(parameters);
		List<Task> tasks = this.setupOrdersTasks(parameters);
		Map<String, List<? extends Resource>> resources = this.setupResources(parameters);
		tasks = this.getUsefulResource(tasks, resources, parameters);

		for (Variable order: orders) {
			order.setValue(tasks);
		}
		Collections.sort(orders);
		orders = this.assignResources(orders, resources, k);

		return orders;
	}

	@Override
	public void recalculateSolutionDetails(List<Solution> offspringSolutions) {
		return ;
	}

	protected List<Variable> setupOrders(Map<Object, Object> parameters) {
		List<Variable> orders = (List<Variable>) parameters.get("orders");

		return orders;
	}

	private List<Task> setupOrdersTasks(Map<Object, Object> parameters) {
		int numberOfTasks = (int) parameters.get("numberOfTasks");
		int [][] relationship = (int[][]) parameters.get("tasks");

		List<Task> tasks = new ArrayList<>();

		for (int i = 0; i < numberOfTasks; i++) {
			Map<String, Object> params = new HashMap<>();

//			double scheduledTime = ((double[]) parameters.get("scheduledTimes"))[i];
			long duration = ((long[]) parameters.get("durations"))[i];
			params.put("id", i);
			params.put("duration", duration);
			params.put("scheduledTime", this.getNow());

			Task task = Task.builder()
					.id(i)
					.duration(duration)
					.scheduledStartTime(this.getNow())
					.descendants(new ArrayList<>())
					.predecessors(new ArrayList<>())
					.requiredHumanResources(new ArrayList<>())
					.requiredMachinesResources(new ArrayList<>())
					.build();
//			task.setTaskAttributes(params);
			tasks.add(task);
		}

		tasks = setupAllTasksNeighbours(tasks, relationship);
		Collections.sort(tasks);
		return tasks;
	}

	private List<Task> setupAllTasksNeighbours(List<Task> tasks, int[][] relationships) {

		int size = tasks.size();

		for (int i = 0; i < size; i++) {
			Task ti = tasks.get(i);
			for (int j = i + 1; j < size; j++) {
				if (i != j) {
					Task tj = tasks.get(j);
					if (relationships[i][j] == 1) {
						ti.getDescendants().add(j);
						tj.getPredecessors().add(i);
					} else if (relationships[j][i] == 1) {
						ti.getPredecessors().add(j);
						tj.getDescendants().add(i);
					}
				}
			}
		}

		return tasks;
	}

	protected Map<String, List<? extends Resource>> setupResources(Map<Object, Object> parameters) {
		Map<String, List<? extends Resource>> resources = new HashMap<>();

		List<HumanResource> humanResources= this.setupHumanResources(parameters);
		List<MachineResource> machineResources = this.setupMachineResources(parameters);

		resources.put("humanResources", humanResources);
		resources.put("machineResources", machineResources);

		return resources;
	}

	protected List<HumanResource> setupHumanResources(Map<Object, Object> parameters) {
		List<HumanResource> resources = new ArrayList<>();
		int numberOfHumanResources = (int) parameters.get("numberOfHumanResources");
		int numberOfSkills = (int) parameters.get("numberOfSkills");
		double [] costs = (double []) parameters.get("humanCosts");
		double [][] lexp = (double [][]) parameters.get("lexp");

		for (int i = 0; i < numberOfHumanResources; i++) {
			HumanResource resource = HumanResource.builder()
					.id(i)
					.cost(costs[i])
					.usedTimeSlots(new ArrayList<>())
					.build();
			List<Skill> skills = new ArrayList<>();

			for (int j = 0; j < numberOfSkills; j++) {
				if (lexp[i][j] > 0) {
					Skill skill = Skill.builder()
							.id(j)
							.experienceLevel(lexp[i][j])
							.build();
					skills.add(skill);
				}
			}
			resource.setSkills(skills);
			resources.add(resource);
		}
		return resources;
	}

	protected List<MachineResource> setupMachineResources(Map<Object, Object> parameters) {
		List<MachineResource> resources = new ArrayList<>();
		int numberOfMachineResources = (int) parameters.get("numberOfMachineResources");
		double [] costs = (double []) parameters.get("machineCosts");

		for (int i = 0; i < numberOfMachineResources; i++) {
			MachineResource resource = MachineResource.builder()
					.id(i)
					.cost(costs[i])
					.build();
			resources.add(resource);
		}

		return resources;
	}

	protected List<Variable> assignResources(List<Variable> orders, Map<String, List<? extends Resource>> resources, double k) {
		return orders;
	}

	private List<Task> getUsefulResource(List<Task> tasks, Map<String, List<? extends Resource>> resources, Map<Object, Object> parameters) {
		// TODO: implement chua hoan thien
		int numberOfHumanResources = (int) parameters.get("numberOfHumanResources");
		double[][] usefulHumanResourcesMap = getUsefulHumaResourcesMap(parameters);
		double[][] usefulMachineResourceMap = (double [][]) parameters.get("mreq");

		int numberOfMachineResources = (int) parameters.get("numberOfMachineResources");
		double[] machineCosts = (double[]) parameters.get("machineCosts");

		List<HumanResource> hResources = (List<HumanResource>) resources.get("humanResources");
		List<MachineResource> mResources = (List<MachineResource>) resources.get("machineResources");

		for (Task task: tasks) {
			List<HumanResource> humanResources = new ArrayList<>();
			List<MachineResource> machineResources = new ArrayList<>();

			// Get useful human resource
			for (int i = 0; i < numberOfHumanResources; i++) {
				if (usefulHumanResourcesMap[task.getId()][i] == 1) {
					HumanResource humanResource = DataUtil.cloneBean(hResources.get(i));
					humanResources.add(humanResource);
				}
			}

			// Get useful machine resource
			for (int i = 0; i < numberOfMachineResources; i++) {
				if (usefulMachineResourceMap[task.getId()][i] == 1) {
					MachineResource machineResourceResource = DataUtil.cloneBean(mResources.get(i));
					machineResources.add(machineResourceResource);
				}
			}

			task.setRequiredHumanResources(humanResources);
			task.setRequiredMachinesResources(machineResources);

		}
		return tasks;
	}

	private double[][] getUsefulHumaResourcesMap(Map<Object, Object> parameters) {
		int[][] treq = (int[][]) parameters.get("treq");
		double [][] lexp = (double [][]) parameters.get("lexp");
		int numberOfHumanResources = (int) parameters.get("numberOfHumanResources");
		int numberOfSkills = (int) parameters.get("numberOfSkills");
		int numberOfTasks = (int) parameters.get("numberOfTasks");

		final int SKILL_IS_USED = 1;
		final int RESOURCE_IS_USEFUL = 1;
		final int RESOURCE_IS_NOT_USEFUL = 0;

		double [][] usefulHumanResourcesMap = new double[numberOfTasks][numberOfHumanResources];

		for (int task = 0; task < numberOfTasks; task++) {
			for (int resource = 0; resource < numberOfHumanResources; resource++) {
				boolean isUseful = true;
				for (int skill = 0; skill < numberOfSkills; skill++) {
					if (treq[task][skill] == SKILL_IS_USED) {
						if (treq[task][skill] + lexp[resource][skill] == SKILL_IS_USED) {
							isUseful = false;
							break;
						}
					}
				}
				if (isUseful == true) {
					usefulHumanResourcesMap[task][resource] = RESOURCE_IS_USEFUL;
				} else {
					usefulHumanResourcesMap[task][resource] = RESOURCE_IS_NOT_USEFUL;
				}
			}
		}
		return usefulHumanResourcesMap;
	}
}
