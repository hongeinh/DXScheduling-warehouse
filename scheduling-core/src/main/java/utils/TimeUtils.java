package utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

	public static final long NON_WORKING_HOURS = 15;
	public static final long WORKING_HOURS = 9;

	public static long calculateTimeDifferenceWithTimeUnit (LocalDateTime firstDateTime, LocalDateTime secondDateTime, ChronoUnit unit) {
		long time = 0;
		if (firstDateTime.isBefore(secondDateTime)) {
			time = firstDateTime.until(secondDateTime, unit);
		} else if (firstDateTime.isAfter(secondDateTime)){
			time = secondDateTime.until(firstDateTime, unit) * (-1);
		}
		return time;
	}

	public static long calculateOfficialElapsedTime(LocalDateTime startTime, LocalDateTime endTime, ChronoUnit unit) {
		long days = ChronoUnit.DAYS.between(startTime, endTime);

		long elapsedTime = unit.between(startTime, endTime);
		if (unit == ChronoUnit.HOURS) {
			elapsedTime -= days * NON_WORKING_HOURS;
		}
		return elapsedTime;
	}

//	public static void main(String[] args) {
//		LocalDateTime first = LocalDateTime.now();
//		LocalDateTime second = LocalDateTime.of(2021, 07, 31, 10, 22);
//		System.out.println((first, second));
//	}
}



