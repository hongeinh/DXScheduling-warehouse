package variable.component.resource;

import lombok.Getter;
import lombok.Setter;
import utils.DataUtil;
import utils.NumberUtil;
import utils.TimeUtils;
import variable.component.timeslot.TimeSlot;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
public class ResourceManager {

	private Map<String, List<? extends Resource>> resourcesMap;

	public <T extends Resource> boolean isResourceAvailable(T requiredResource, LocalDateTime time) {

		List<TimeSlot> usedTimeSlot = requiredResource.getUsedTimeSlots();
		Collections.sort(usedTimeSlot);
		int timeSlotSize = usedTimeSlot.size();
		boolean timeSlotAvailable = false;

		for (int i = 0; i < timeSlotSize; i++) {
			TimeSlot slot = usedTimeSlot.get(i);
//			if (slot.get)
		}


		return false;
	}

	/**
	 * Get an available resource from list with a predefined start time.
	 * If no resource is available, return null;
	 * @param requiredResources
	 * @param time
	 * @param <T>
	 * @return
	 */
	public <T extends Resource> T getAvailableResource(List<T> requiredResources, LocalDateTime time) {
		// TODO
		if (requiredResources.isEmpty()) {
			return null;
		}
		List<T> availableResources = new ArrayList<>();
		List<T> possibleResourceList = new ArrayList<>();
		for (Map.Entry<String, List<? extends Resource>> entry : resourcesMap.entrySet()) {
			List<? extends Resource> resourceList = entry.getValue();

			// only check if same kind of resource
			if (requiredResources.get(0).getClass().equals(resourceList.get(0).getClass())) {
				for (T resource : requiredResources) {
					if (isResourceAvailable(resource, resourceList, time)) {
						availableResources.add(resource);
					} else {
						possibleResourceList.add((T) resourceList.get(resource.getId()));;
					}
				}
				break;
			} else {
				continue;
			}
		}

		T availableResource = chooseRandomAvailableResource(availableResources);
		if (availableResource != null) {
			TimeSlot timeSlot = new TimeSlot(time, null);
			timeSlot = TimeUtils.getValidTimeSlot(timeSlot);
			availableResource.getUsedTimeSlots().add(timeSlot);
		} else {
			availableResource = calculateNextAvailableResource(possibleResourceList);
		}
		return availableResource;
	}

	/**
	 * Get a random resource
	 * @param resourceList
	 * @param <T>
	 * @return
	 */
	private <T extends Resource> T calculateNextAvailableResource(List<T> resourceList) {
		int rand = NumberUtil.getRandomIntNumber(0, resourceList.size() - 1);
		T randomResource = resourceList.get(rand);
		T returnResource = DataUtil.cloneBean(randomResource);
		List<TimeSlot> randomResourceTimeSlot = randomResource.getUsedTimeSlots();
		returnResource.getUsedTimeSlots().clear();
		TimeSlot availableTimeSlot = new TimeSlot(randomResourceTimeSlot.get(randomResourceTimeSlot.size() - 1).getEndDateTime(), null);
		returnResource.getUsedTimeSlots().add(availableTimeSlot);
		return returnResource;
	}

	/**
	 * Select resource to assign from a list of available resources.
	 * If list is empty, no resource is available.
	 * @param availableResources
	 * @param <T>
	 * @return
	 */
	private <T extends Resource> T chooseRandomAvailableResource(List<T> availableResources) {
		if (availableResources.isEmpty()) {
			return null;
		} else {
			int rand = NumberUtil.getRandomIntNumber(0, availableResources.size() - 1);
			T randomResource = availableResources.get(rand);
			return DataUtil.cloneBean(randomResource);
		}
	}


	/**
	 * Checks if a specific resource is available with a predefined start time
	 * @param requiredResource
	 * @param resources
	 * @param time
	 * @param <T>
	 * @return
	 */
	private <T extends Resource> boolean isResourceAvailable(T requiredResource, List<? extends Resource> resources, LocalDateTime time) {
		for (Resource resource: resources) {
			if (resource.getId() == requiredResource.getId()) {
				List<TimeSlot> timeSlots = resource.getUsedTimeSlots();

				if (timeSlots == null || timeSlots.isEmpty()) {
					return true;
				}

				LocalDateTime lastEndTime = (!timeSlots.isEmpty()) ?
						timeSlots.get(timeSlots.size() - 1).getEndDateTime() : null ;
				if (lastEndTime == null || lastEndTime.isBefore(time) || lastEndTime.isEqual(time)) {
					return true;
				}
			} else {
				continue;
			}
		}
		return false;
	}

	/**
	 * Add resource's used time to resource manager's resource so that it can update
	 *
	 * @param resource
	 * @param <T>
	 */
	public <T extends Resource> void addTimeSlot(T resource) {
		TimeSlot timeSlot = resource.getUsedTimeSlots().get(0);

		boolean isAdded = false;
		for (Map.Entry<String, List<? extends Resource>> entry : resourcesMap.entrySet()) {
			List<? extends Resource> resourceList = entry.getValue();
			if (resource.getClass().equals(resourceList.get(0).getClass())) {
				for (Resource r : resourceList) {
					if (resource.getId() == r.getId()) {
						r.getUsedTimeSlots().add(timeSlot);
						Collections.sort(r.getUsedTimeSlots());
						isAdded = true;
						break;
					}
				}
				break;
			} else {
				continue;
			}
		}

		if (isAdded == false) {
			System.out.println("No resource found to add time slot");
		}
	}


}
