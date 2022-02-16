package variable.impl;

import utils.DataUtil;
import variable.Variable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import variable.component.resource.impl.HumanResource;
import variable.component.resource.impl.MachineResource;
import variable.component.timeslot.TimeSlot;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
public class Order implements Variable{

	private int id;
	private double priority;
	private double weight;
	private double volume;
	private double penaltyRate;
	private double totalTimeAllowed;
	private double totalTimeSpent;
	private double totalCost;

	List<Task> tasks;

	@Override
	public Object getValue() {
		return tasks;
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof List) {
			this.tasks = (List<Task>) value;
		}
	}

	public double getTotalTimeSpent() {
		double totalTime = 0;
		for (Task task: tasks) {
			totalTime += task.getDuration();
		}
		return totalTime;
	}

	public double getTotalCost() {
		double totalCost = 0;
		for (Task task: tasks) {
			totalCost += task.getTaskCost();
		}
		return totalCost;
	}
	@Override
	public int compareTo(Variable o) {
		if (o instanceof Order) {
			Order otherOrder = (Order) o;
			return this.totalTimeAllowed > otherOrder.getTotalTimeAllowed()? 1 :
						(this.totalTimeAllowed ==otherOrder.getTotalTimeAllowed()? 0 : -1);
		}
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
//		String DELIMETER = ",";
//		stringBuilder.append(this.id + DELIMETER +
//						this.getTotalTimeAllowed() + DELIMETER +
//						this.getTotalTimeSpent() + DELIMETER +
//						this.getTotalCost() + DELIMETER);
		for (Task task: tasks) {
			stringBuilder.append(task.toString() + "\n");
		}

		return stringBuilder.toString();
	}

	/**
	 * Calculate the possible start time of a task based on its predecessors' end time.
	 * Max end time is the scheduled start time
	 * @param task
	 * @return
	 */
	public LocalDateTime calculateTaskScheduledStartTime(Task task) {

		// Step 1: Get list of predecessors ID
		List<Integer> predecessorsIds = task.getPredecessors();

		if (predecessorsIds.isEmpty()) {
			return null;
		}
		LocalDateTime scheduledStartTime = LocalDateTime.now();
		List<Task> predecessors = this.tasks.stream()
				.filter(predecessorTask -> predecessorsIds.contains(predecessorTask.getId()))
				.collect(Collectors.toList());

		List<LocalDateTime> dateTimes = predecessors.stream()
				.map(predecessor -> predecessor.getEndTime())
				.collect(Collectors.toList());
		Collections.sort(dateTimes);

		// Get max date time end
		scheduledStartTime = dateTimes.get(dateTimes.size() - 1);
		return scheduledStartTime;
	}

	public LocalDateTime getStartTime() {
		LocalDateTime startTime = this.tasks.get(0).getStartTime();
		return startTime;
	}

	public LocalDateTime getEndTime() {
		LocalDateTime maxEndTime = this.getStartTime();
		for (Task task: this.tasks) {
			if (maxEndTime.isBefore(task.getEndTime())) {
				maxEndTime = task.getEndTime();
			}
		}

		return maxEndTime;
	}


	public long getElapsedTimeInUnit(ChronoUnit unit) {
		long elapsedTime = unit.between(this.getStartTime(), this.getEndTime());

		return elapsedTime;
	}

	public Map<Integer, Long> getHumanResourcesWorkingTime (ChronoUnit unit) {
		List<HumanResource> resources = new ArrayList<>();
		List<Integer> resourceIds = new ArrayList<>();
		Map<Integer, Long> resourcesWorkingTime = new HashMap<>();
		// Get list of human resources and time slots
		for (Task task: this.tasks) {
			List<HumanResource> taskHumanResources = task.getRequiredHumanResources();
			for (HumanResource humanResource: taskHumanResources) {
				if (!resourceIds.contains(humanResource.getId())) {
					resourceIds.add(humanResource.getId());
					resources.add(DataUtil.cloneBean(humanResource));
				} else {
					resources.get(humanResource.getId())
							.addTimeSlots(humanResource.getUsedTimeSlots());
				}
			}
		}

		// Sort by ID
		Collections.sort(resources);

		// Calculate working time from resources:
		for (HumanResource resource: resources) {
			List<TimeSlot> timeSlots = resource.getUsedTimeSlots();
			long workingTime = 0;
			for (TimeSlot timeSlot: timeSlots) {
				workingTime += unit.between(timeSlot.getStartDateTime(), timeSlot.getEndDateTime());
			}
			resourcesWorkingTime.put(resource.getId(), workingTime);
		}
		return resourcesWorkingTime;
	}

	public Map<Integer, Long> getMachineResourcesWorkingTime (ChronoUnit unit) {
		List<MachineResource> resources = new ArrayList<>();
		List<Integer> resourceIds = new ArrayList<>();
		Map<Integer, Long> resourcesWorkingTime = new HashMap<>();
		// Get list of machine resources
		for (Task task: this.tasks) {
			List<MachineResource> taskHumanResources = task.getRequiredMachinesResources();
			for (MachineResource humanResource: taskHumanResources) {
				if (!resourceIds.contains(humanResource.getId())) {
					resourceIds.add(humanResource.getId());
					resources.add(DataUtil.cloneBean(humanResource));
				} else {
					resources.get(humanResource.getId())
							.addTimeSlots(humanResource.getUsedTimeSlots());
				}
			}
		}

		// Sort by ID
		Collections.sort(resources);

		// Calculate working time from resources:
		for (MachineResource resource: resources) {
			List<TimeSlot> timeSlots = resource.getUsedTimeSlots();
			long workingTime = 0;
			for (TimeSlot timeSlot: timeSlots) {
				workingTime += unit.between(timeSlot.getStartDateTime(), timeSlot.getEndDateTime());
			}
			resourcesWorkingTime.put(resource.getId(), workingTime);
		}

		return resourcesWorkingTime;
	}
}
