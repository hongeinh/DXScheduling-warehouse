package variable.component.resource;

import common.STATUS;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import variable.component.timeslot.TimeSlot;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Resource implements Serializable {

	public static int resourceId = 0;
	protected int id;
	protected STATUS status;
	protected double cost;
	protected List<TimeSlot> usedTimeSlots;

	public List<TimeSlot> addTimeSlot(TimeSlot timeSlot) {
		usedTimeSlots.add(timeSlot);
		Collections.sort(usedTimeSlots);
		return usedTimeSlots;
	}

	public List<TimeSlot> removeTimeSlot(TimeSlot timeSlot) {
		usedTimeSlots.remove(timeSlot);
		Collections.sort(usedTimeSlots);
		return usedTimeSlots;
	}
}