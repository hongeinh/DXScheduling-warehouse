package variable.component.resource.impl;

import common.STATUS;
import common.TYPE;
import lombok.*;
import variable.component.resource.Resource;
import variable.component.skill.Skill;
import variable.component.timeslot.TimeSlot;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HumanResource extends Resource{
	private List<Skill> skills;

	@Builder
	public HumanResource(int id, STATUS status, double cost, List<TimeSlot> usedTimeSlots, List<Skill> skills) {
		super(id, status, cost, usedTimeSlots);
		this.skills = skills;
	}

	public double getAverageExp() {
		double avgExp = 0;
		for (Skill skill: skills) {
			avgExp += skill.getExperienceLevel();
		}
		return avgExp/skills.size();
	}
}
