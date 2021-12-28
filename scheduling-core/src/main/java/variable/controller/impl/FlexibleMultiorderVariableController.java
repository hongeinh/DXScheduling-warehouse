package variable.controller.impl;

import common.STATUS;
import variable.Variable;
import variable.component.resource.Resource;
import variable.component.resource.ResourceManager;
import variable.component.resource.impl.HumanResource;
import variable.component.resource.impl.MachineResource;
import variable.component.timeslot.TimeSlot;
import variable.impl.Order;
import variable.impl.Task;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class FlexibleMultiorderVariableController extends FixedMultiorderVariableController {

	ResourceManager resourceManager;

	public FlexibleMultiorderVariableController() {
		this.resourceManager = new ResourceManager();
	}

	/**
	 * Assign resources to all orders.
	 * @param orders
	 * @param resources
	 * @param k
	 * @return
	 */
	@Override
	protected List<Variable> assignResources(List<Variable> orders, Map<String, List<? extends Resource>> resources, double k) {
		this.resourceManager.setResourcesMap(resources);

		int taskSize = ((List) orders.get(0).getValue()).size();
		int orderSize = orders.size();

		LocalDateTime currentTimeSlot = now;
		for (int i = 0; i < taskSize; i++) {
			for (int j = 0; j < orderSize; j++) {
				Order order = (Order) orders.get(j);
				Task task = order.getTasks().get(i);

				// Tinh thoi gian co the bat dau
				LocalDateTime scheduledStartTime = order.calculateTaskScheduledStartTime(task);
				scheduledStartTime = (scheduledStartTime != null) ? scheduledStartTime : currentTimeSlot;

				// Kiem tra resource available
				HumanResource humanResource = resourceManager.getAvailableResource(task.getRequiredHumanResources(), scheduledStartTime);
				MachineResource machineResource = resourceManager.getAvailableResource(task.getRequiredMachinesResources(), scheduledStartTime);

				LocalDateTime hResourceStart = humanResource.getUsedTimeSlots().get(0).getStartDateTime();
				LocalDateTime mResourceStart = humanResource.getUsedTimeSlots().get(0).getStartDateTime();

				// Check HumanResource va Machine resource cai nao co sau thi start time theo cai do
				scheduledStartTime = hResourceStart.isBefore(mResourceStart) ? hResourceStart : mResourceStart;
				LocalDateTime endTime = scheduledStartTime.plus(task.getDuration(), ChronoUnit.HOURS);

				// Set thoi gian cho task
				task.setStartTime(scheduledStartTime);
				task.setEndTime(endTime);

				// Set lai thoi gian cho resource
				humanResource.getUsedTimeSlots().clear(); machineResource.getUsedTimeSlots().clear();
				TimeSlot timeSlot = new TimeSlot(scheduledStartTime, endTime);
				humanResource.getUsedTimeSlots().add(timeSlot); humanResource.setStatus(STATUS.USEFUL);
				machineResource.getUsedTimeSlots().add(timeSlot);machineResource.setStatus(STATUS.USEFUL);

				task.getRequiredHumanResources().clear();task.getRequiredMachinesResources().clear();
				task.getRequiredHumanResources().add(humanResource); task.getRequiredMachinesResources().add(machineResource);

				this.resourceManager.addTimeSlot(humanResource);
				this.resourceManager.addTimeSlot(machineResource);

			}
		}

		return orders;
	}
}
