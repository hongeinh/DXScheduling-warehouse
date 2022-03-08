package variable.impl;

import common.STATUS;
import utils.DataUtil;
import variable.component.resource.impl.HumanResource;
import variable.component.resource.impl.MachineResource;
import variable.component.resource.Resource;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import variable.component.skill.Skill;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
public class Task implements Comparable<Task>, Serializable {

	private int orderId;
	private int id;
	private LocalDateTime scheduledStartTime;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private long duration;
	private double idle;
	private double priority;
	private List<Integer> predecessors;
	private List<Integer> descendants;
	private List<Integer> skills;
	private List<MachineResource> requiredMachinesResources;
	private List<HumanResource> requiredHumanResources;


	private String getAssignedResourceIdsString(List<? extends Resource> resources, String delimeter) {
		List<String> resourcesId = resources.stream()
				.filter(resource -> resource.getStatus() == STATUS.ASSIGNED)
				.map(resource -> Integer.toString(resource.getId()))
				.collect(Collectors.toList());
		return resourcesId.stream().map(String::valueOf).collect(Collectors.joining(delimeter));
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		String DELIMETER = ", ";

		stringBuilder.append(this.id + DELIMETER);

		stringBuilder.append(getAssignedResourceIdsString(requiredHumanResources, " "));
		stringBuilder.append(DELIMETER);
		stringBuilder.append(getAssignedResourceIdsString(requiredMachinesResources, " "));
		return stringBuilder.toString();
	}

	@Override
	public int compareTo(Task o) {
		if (this.getDescendants().contains(o)) {
			return -1;
		} else if (o.getDescendants().contains(this)) {
			return 1;
		} else {
			int compare = 0;
			if (this.scheduledStartTime.isBefore(o.getScheduledStartTime()))
				compare = 1;
			else if (this.scheduledStartTime.isAfter(o.getScheduledStartTime()))
				compare = 0;
			return compare;
		}
	}


	public void setTaskAttributes(Object value) {
		if (value instanceof HashMap ) {
			HashMap<String, Object> parameters = (HashMap <String, Object>) value;
			this.id = (Integer) parameters.get("id");
			this.duration =  (Long) parameters.get("duration");
			this.scheduledStartTime = (LocalDateTime) parameters.get("scheduledTimes");
		}
	}

	@Deprecated
	public double getMaxExperienceForSkill(int i) {
		double maxExperience = 0.0;
		for(Resource resource: requiredHumanResources) {
			double exp = ((HumanResource) resource).getSkills().get(i).getExperienceLevel();
			maxExperience = (maxExperience > exp) ? maxExperience : exp;
		}

		return maxExperience;
	}

	@Deprecated
	public double getTotalExperienceForSkill(int i) {
		double totalExperience = 0.0;
		for(Resource resource: requiredHumanResources) {
			totalExperience += ((HumanResource) resource).getSkills().get(i).getExperienceLevel();
		}
		return totalExperience;
	}

	@Deprecated
	public int getNumberOfAssignedResources() {
		int resourceCount = 0;
		for (Resource Resource: requiredHumanResources) {
			if (Resource.getStatus() == STATUS.ASSIGNED)
				resourceCount++;
		}
		return resourceCount;
	}

	public double getTaskCost() {
		return getHumanResourceCost() + getMachineResourceCost();
	}

	private double getMachineResourceCost() {
		List<MachineResource> assignedResources = this.getRequiredMachinesResources().stream()
				.filter(machineResource -> machineResource.getStatus() == STATUS.ASSIGNED)
				.collect(Collectors.toList());
		double totalCost = 0;
		for (MachineResource resource: assignedResources) {
			totalCost += resource.getCost() * this.getDuration();
		}

		return totalCost;
	}

	private double getHumanResourceCost() {
		List<HumanResource> assignedResources = this.getRequiredHumanResources().stream()
				.filter(humanResource -> humanResource.getStatus() == STATUS.ASSIGNED)
				.collect(Collectors.toList());

		double totalCost = 0;

		for (HumanResource assignedResource: assignedResources) {
			totalCost += assignedResource.getCost() * this.getDuration();
		}
		return totalCost;
	}

	public double getAverageExperience() {
		double totalExperience = 0;
		double assignedResource = 0;
		for (HumanResource humanResource: this.getRequiredHumanResources()) {
			if (humanResource.getStatus() == STATUS.ASSIGNED) {
				totalExperience += humanResource.getAverageExp();
			}
			assignedResource++;
		}
		return  totalExperience/assignedResource;
	}

	public String getHumanResourceString() {
		return getAssignedResourceIdsString(requiredHumanResources, " ");
	}

	public String getMachineResourceString() {
		return getAssignedResourceIdsString(requiredMachinesResources, " ");
	}

	public double getNumberOfUsefulSkills() {
		List<Skill> skills = this.getRequiredHumanResources().get(0).getSkills();
		int count = 0;
		for (Skill skill: skills) {
			if (skill.getExperienceLevel() > 0) {
				count++;
			}
		}
		return count;
	}

	public int changeHumanResourceStatus(HumanResource humanResource, STATUS assigned) {
		humanResource.setStatus(assigned);
		int size = this.requiredHumanResources.size();
		int index = 0;
		for (int i = 0; i < size; i++) {
			if (this.requiredHumanResources.get(i).getId() == humanResource.getId()) {
				this.requiredHumanResources.remove(i);
				this.requiredHumanResources.add(i, humanResource);
				index = i;
			}
		}
		return index;
	}

	public void changeResourcePosition(int index, int i) {
		HumanResource humanResource = DataUtil.cloneBean(this.requiredHumanResources.get(index));
		this.requiredHumanResources.remove(index);
		this.requiredHumanResources.add(0, humanResource);

	}
}
