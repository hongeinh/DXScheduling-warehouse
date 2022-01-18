package variable.component.resource;

import common.STATUS;
import common.TYPE;
import lombok.*;
import variable.Variable;
import variable.component.timeslot.TimeSlot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resource implements Serializable, Comparable<Resource> {

	public static int resourceId = 0;
	protected int id;
	protected STATUS status;
	protected TYPE type = TYPE.DEFAULT;
	protected double cost;
	protected List<TimeSlot> usedTimeSlots;

	public List<TimeSlot> addTimeSlots(List<TimeSlot> timeSlots) {
		usedTimeSlots.addAll(timeSlots);
		Collections.sort(usedTimeSlots);
		return usedTimeSlots;
	}

	public List<TimeSlot> removeTimeSlot(TimeSlot timeSlot) {
		usedTimeSlots.remove(timeSlot);
		Collections.sort(usedTimeSlots);
		return usedTimeSlots;
	}

	public boolean equals(Object o) {
		if (o instanceof Resource) {
			if (((Resource) o).getId() == this.getId()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int compareTo(Resource o) {
		if(this.id < o.getId()) {
			return -1;
		} else if (this.id > o.getId()) {
			return 1;
		} else {
			return 0;
		}
	}
}