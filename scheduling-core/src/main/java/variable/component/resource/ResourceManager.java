package variable.component.resource;

import lombok.Getter;
import lombok.Setter;
import utils.DataUtil;
import utils.ObjectUtil;
import variable.component.timeslot.TimeSlot;

import java.sql.Time;
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
		//TODO
		List<T> availableResources = new ArrayList<>();
		List<T> possibleResourceList = null;
		for (Map.Entry<String, List<? extends Resource>> entry : resourcesMap.entrySet()) {
			List<? extends Resource> resourceList = entry.getValue();

			// only check if same kind of resource
			if (requiredResources.get(0).getClass().equals(resourceList.get(0).getClass())) {
				for (T resource : requiredResources) {
					if (isResourceAvailable(resource, resourceList, time)) {
						availableResources.add(resource);
					} else {
						List<TimeSlot> timeSlots = resource.getUsedTimeSlots();
						Collections.sort(timeSlots);
					}
				}
				ObjectUtil.copyProperties(resourceList, possibleResourceList);
				// finish when done
				break;
			} else {
				continue;
			}
		}

		T availableResource = chooseRandomAvailableResource(availableResources);
		if (availableResource != null) {
			availableResource.getUsedTimeSlots().add(new TimeSlot(time, null));
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
		Random rand = new Random();
		T randomResource = resourceList.get(rand.nextInt(resourceList.size()));
		T returnResource = DataUtil.cloneBean(randomResource);
		List<TimeSlot> randomResourceTimeSlot = randomResource.getUsedTimeSlots();
		returnResource.getUsedTimeSlots().clear();
		returnResource.getUsedTimeSlots().add(new TimeSlot(randomResourceTimeSlot.get(randomResourceTimeSlot.size() - 1).getStartDateTime(), null));
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
			Random rand = new Random();
			T randomResource = availableResources.get(rand.nextInt(availableResources.size()));
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

				if (timeSlots.isEmpty()) {
					return true;
				}

				LocalDateTime lastEndTime = timeSlots.get(timeSlots.size() - 1).getEndDateTime();
				if (lastEndTime.isBefore(time) || lastEndTime.isEqual(time)) {
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
			} else {
				continue;
			}
		}

		if (isAdded == false) {
			System.out.println("No resource found to add time slot");
		}
	}
}
