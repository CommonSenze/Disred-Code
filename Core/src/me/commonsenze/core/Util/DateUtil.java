package me.commonsenze.core.Util;

public class DateUtil {

	public enum MaxConverter {
		SECONDS, MINUTES, HOURS, DAYS;
	}
	/**
	 * Converts a {@link System#currentTimeMillis()} time difference to a formatted time.
	 * @param start - the starting time to start from.
	 * @param end - the ending time
	 * @return an array of the time formatted.
	 * [0] = seconds [1] = minutes [2] = hours [3] = days
	 */
	public static int[] convertElapsedTime(long start, long end, MaxConverter converter) {
		long time = Math.abs(end - start);
		int seconds = (int) (converter == MaxConverter.SECONDS ? (time /1000) : (time /1000) %60);
		int minutes = (int) (converter == MaxConverter.MINUTES ? (time / (1000*60)) : (time / (1000*60)) % 60);
		int hours = (int) (converter == MaxConverter.HOURS ? (time / (1000*60*60)) : (time / (1000*60*60)) % 24);
		int days = (int) (time / (1000*60*60*24));

		switch (converter) {
		case SECONDS:
			minutes = -1;
		case MINUTES:
			hours = -1;
		case HOURS:
			days = -1;
		default:
			break;
		}

		return new int[]{seconds, minutes, hours, days};
	}
}
