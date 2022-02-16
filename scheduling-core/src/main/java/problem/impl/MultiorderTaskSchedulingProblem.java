package problem.impl;

import common.STATUS;
import variable.controller.VariableController;
import variable.component.resource.impl.HumanResource;
import variable.component.resource.impl.MachineResource;
import variable.component.skill.Skill;
import variable.Variable;
import variable.impl.Order;
import variable.impl.Task;
import representation.Solution;
import utils.TimeUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiorderTaskSchedulingProblem extends TaskSchedulingResourceAllocatingProblem {
	/**
	 * This method will return a list of variables to the algorithm to find its solution
	 * parameters 		 	Map
	 * treq:				int[][] 		Task - Skill matrix
	 * lexp: 				double[][]		Resource - Skill matrix
	 * tasks: 				int[][]			Task matrix
	 * numberOfTasks: 		int				number of tasks in this problem
	 * numberOfHumanResources: 	int			number of resources in this problem
	 * numberOfSkills: 		int				number of skills in this problem
	 * scheduledTimes: 		List<Integer>	list of scheduled time for variables
	 * durations: 			List<Integer>	list of durations for variables
	 * numberOfObjectives:	int				the number of objectives needed to calculate for solutions.
	 * numberOfFitness:		int				the number of fitness for this solution
	 * maxDuration			int				maximum duration for all tasks
	 *
	 * @param params
	 * @param variableController
	 */
	public MultiorderTaskSchedulingProblem(Map<Object, Object> params, VariableController variableController) {
		super(params, variableController);
	}

	/**
	 * First objective: Time
	 * Second objective: Quality
	 * Third objective: Cost (human cost + machine cost + penalty cost)
	 * */
	@Override
	public Solution evaluate(Solution solution) {
		solution = evaluateIdleDuration(solution);
		solution = evaluateQuality(solution);
		solution = evaluateTotalCost(solution);
//		solution = evaluateWorkBalance(solution);
		return solution;
	}



	/**
	 * Counts the total idle time between tasks in one order and between orders
	 *
	 * */
	public Solution evaluateIdleDuration(Solution solution) {

		List<Variable> variables = solution.getVariables();

		double totalDelay = 0;
		int variableSize = variables.size();

		for (int i = 0; i < variableSize; i++) {
			List<Task> tasks = (List<Task>) variables.get(i).getValue();
			totalDelay += calculateEachOrderIdleTime(tasks);
			if (i != 0) {
				totalDelay += calculateElapsedTimeUntilNextOrder(variables.get(i - 1), variables.get(i));
			}
		}
//		solution.getObjectives()[0] = totalDelay/variableSize;
		solution.getObjectives()[0] = totalDelay;

		return solution;
	}

	private long calculateElapsedTimeUntilNextOrder(Variable variable, Variable variable1) {
		Task firstTask1 = ((List<Task>) variable.getValue()).get(0);
		Task firstTask2 = ((List<Task>) variable1.getValue()).get(0);

		LocalDateTime start1 = firstTask1.getStartTime();
		LocalDateTime start2 = firstTask2.getStartTime();
		long delay = TimeUtils.calculateTimeDifferenceWithTimeUnit(start1, start2, ChronoUnit.MINUTES);
		return delay > 0 ? delay : 0;
	}

	/**
	 * Calculate the idle time between the scheduled start and the actual start of each task in the same order
	 * */
	private double calculateEachOrderIdleTime(List<Task> tasks) {
		double delay = 0;
		for (Task task: tasks) {
			double idle = TimeUtils.calculateTimeDifferenceWithTimeUnit(task.getScheduledStartTime(), task.getStartTime(), ChronoUnit.MINUTES);
			idle = idle <= 0 ? 0 : (1/(1 + idle));
			delay += idle;
			task.setIdle(idle);
		}

		return delay;
	}

	/**
	 * Evaluate the total cost for each order based on its human cost, machine cost and penalty cost when finishing time is after deadline
	 * */
	public Solution evaluateTotalCost(Solution solution) {
		List<Variable> variables = solution.getVariables();
		double totalCostAllOrders = 0.0;

		for (Variable variable: variables) {
			Order order = (Order) variable;
			List<Task> tasks = order.getTasks();
			double totalCost = 0;

			for(Task task: tasks) {

				double taskHumanCost = calculateHumanCost( task.getRequiredHumanResources(), task.getDuration());

				double taskMachineCost = calculateMachineCost(task.getRequiredMachinesResources(), task.getDuration());

				double totalTimeSpent = order.getTotalTimeSpent();
				double totalTimeAllowed = order.getTotalTimeAllowed();
				double penaltyCost = totalTimeSpent <= totalTimeAllowed ? 0 : (totalTimeSpent - totalTimeAllowed);

				penaltyCost *= order.getPenaltyRate();

				totalCost += taskHumanCost + taskMachineCost + penaltyCost;
			}
			totalCostAllOrders += totalCost;
			order.setTotalCost(totalCost);
		}
		solution.getObjectives()[2] = totalCostAllOrders;
		return solution;
	}


	private double calculateMachineCost(List<MachineResource> machineResources, double duration) {
		double taskMachineCost = 0;
		for (MachineResource machine: machineResources) {
			if (machine.getStatus() == STATUS.ASSIGNED) {
				double machineCost = machine.getCost() * duration * machine.getConsumeFactor();
				taskMachineCost += machineCost;
			}
		}
		return taskMachineCost;
	}
	private double calculateHumanCost(List<HumanResource> resources, double duration) {
		double taskHumanCost = 0;
		for (HumanResource resource: resources) {
			if (resource.getStatus() == STATUS.ASSIGNED) {
				double resourceCost = resource.getCost();
				double totalLexp = 0;
				List<Skill> skills = resource.getSkills();
				for (Skill skill: skills) {
					totalLexp += skill.getExperienceLevel();
				}
				taskHumanCost += resourceCost * totalLexp;
			}
		}
		return taskHumanCost * duration;
	}

	/**
	 * This function evaluates the quality of each order in a solution based on the order's complete time against its deadline
	 * @param solution	The Solution to be evaluated
	 *
	 *
	 * */
	private Solution evaluateQuality(Solution solution) {
		List<Variable> variables = solution.getVariables();
//		double timeQuality = 0;
//		for (Variable variable: variables) {
//			Order order = (Order) variable;
//			double totalTimeSpent = order.getTotalTimeSpent();
//			double totalTimeAllowed = order.getTotalTimeAllowed();
//			if (totalTimeAllowed == 0 || totalTimeSpent == 0)
//				continue;
//			timeQuality += totalTimeSpent <= totalTimeAllowed ? 1 : (totalTimeAllowed/totalTimeSpent);
//		}
//		solution.getObjectives()[1] = timeQuality/variables.size();
		double avgQuality = 0;
		for (Variable variable: variables) {
			Order order = (Order) variable;
			List<Task> tasks = order.getTasks();
			double orderQuality = 0;
			for (Task task: tasks) {
				List<HumanResource> humanResources = task.getRequiredHumanResources();
				for (HumanResource humanResource: humanResources) {
					double experience = humanResource.getAverageExp();
					orderQuality += experience;
				}
				orderQuality /= task.getNumberOfUsefulSkills();
			}
			avgQuality += orderQuality/tasks.size();
		}
		solution.getObjectives()[1] = variables.size() / avgQuality;
		return solution;
	}


	/**
	 * This function evaluates the the work balance of staffs in a solution based on the longest order's completion time.
	 * The work balance of each solution is the average work time of staff / total work time
	 * @param solution	The Solution to be evaluated
	 *
	 *
	 * */
	private Solution evaluateWorkBalance(Solution solution) {
		List<Variable> variables = solution.getVariables();
		LocalDateTime earliestStartTime = ((Order) variables.get(0)).getStartTime();
		LocalDateTime latestEndTime = this.getSolutionLatestEndTime(variables, earliestStartTime);
		double elapsedTime = ChronoUnit.HOURS.between(earliestStartTime, latestEndTime);

		double averageWorkingTime = getResourcesAverageWorkingTime(variables);

//		solution.getObjectives()[1] = averageWorkingTime/elapsedTime;
		// the longer the working time, the smaller the objective -> goal
		solution.getObjectives()[1] = elapsedTime/averageWorkingTime;
		return solution;

	}

	private double getResourcesAverageWorkingTime(List<Variable> variables) {
		int resourceCount = 0;
		Map<Integer, Long> resourceWorkingTime = new HashMap<>();

		// Get the total working time of each resource for all variables
		for (Variable variable: variables) {
			Map<Integer, Long> workingTime = ((Order) variable).getHumanResourcesWorkingTime(ChronoUnit.HOURS);
			if (resourceWorkingTime.isEmpty()) {
				resourceWorkingTime.putAll(workingTime);
			} else {
				for (Map.Entry<Integer, Long> entry: workingTime.entrySet()) {
					long time = resourceWorkingTime.get(entry.getKey());
					resourceWorkingTime.put(entry.getKey(), time + entry.getValue());
				}
			}
		}

		// Get the average working time for all resources.
		double average = 0;
		for (Map.Entry<Integer, Long> entry: resourceWorkingTime.entrySet()) {
			average += entry.getValue();
		}
		return average/resourceWorkingTime.size();
	}

	private LocalDateTime getSolutionLatestEndTime(List<Variable> variables, LocalDateTime earliestStartDate) {
		LocalDateTime maxEndTime = earliestStartDate;
		for (Variable variable: variables) {
			if (maxEndTime.isBefore(((Order) variable).getEndTime())) {
				maxEndTime = ((Order) variable).getEndTime();
			}
		}
		return null;
	}

	@Override
	public double[] evaluateConstraints(Solution solution) {
		double[] constraints = new double[2];
//		constraints[0] = evaluateResourceContraints(solution);
		constraints[1] = evaluateTimeConstraints(solution);
		return constraints;
	}

	private double evaluateTimeConstraints(Solution solution) {
		boolean isValidTime = true;
		List<Variable> variables = solution.getVariables();
		for (Variable variable: variables) {
			List<Task> tasks = (List<Task>) variable.getValue();
			if (!isValidTaskTimeContraints(tasks)) {
				isValidTime = false;
				break;
			}
		}
		return isValidTime == true ? 1 : 0;
		
	}

	private boolean isValidTaskTimeContraints(List<Task> tasks) {
		for (Task task: tasks) {
			if (task.getDuration() >= 8) {
				return false;
			}
		}
		return true;
	}

	private double evaluateResourceContraints(Solution solution) {
		int numberOfHumanResource = (int) this.parameters.get("numberOfHumanResources");
		int numberMachineResource = (int) this.parameters.get("numberOfMachineResources");

		double humanResourceConflict = this.humanResourceConflictHelper.evaluateResourceConflict(solution, numberOfHumanResource);
		double machineResourceConflict = this.machineResourceConflictHelper.evaluateResourceConflict(solution, numberMachineResource);

		return humanResourceConflict + machineResourceConflict;
	}

}
