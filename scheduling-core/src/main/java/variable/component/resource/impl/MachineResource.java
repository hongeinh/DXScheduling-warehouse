package variable.component.resource.impl;

import common.STATUS;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import variable.component.resource.Resource;
import variable.component.timeslot.TimeSlot;

import java.util.List;

@Getter
@Setter
public class MachineResource extends Resource{

	private double consumeFactor;

	@Builder
	public MachineResource(int id, STATUS status, double cost, List<TimeSlot> usedTimeSlots, double consumeFactor) {
		super(id, status, cost, usedTimeSlots);
		this.consumeFactor = consumeFactor;
	}
}
