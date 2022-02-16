package utils;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class NumberUtil {
	public static int getRandomIntNumber(int min, int max) {
		double rand = getRandomDoubleNumber(min, max);
		double floored = Math.floor(rand);
		if (rand - floored >= 0.5) {
			return (int) rand;
		} else {
			return (floored > 0) ? ((int) floored - 1) : ((int) floored);
		}
	}

	public static double floor2DecimalPoints(double num) {
		BigDecimal bd = new BigDecimal(num).setScale(2, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static double getRandomDoubleNumber(double min, double max) {
		return ThreadLocalRandom.current().nextDouble(min, max + 1);
	}
}
