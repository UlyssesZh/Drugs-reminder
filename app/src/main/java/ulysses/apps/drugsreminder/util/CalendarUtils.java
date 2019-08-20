package ulysses.apps.drugsreminder.util;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class CalendarUtils {
	/** Uses {@link Log} to print a message with date information provided.
	 * @param calendar provides the information about to be printed
	 * @param messageFormat the format of the message. Must include a "%s" in it.*/
	public static void print(@NotNull Calendar calendar, String messageFormat) {
		Log.d("CalendarUtils", String.format(messageFormat,
				String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d",
						calendar.get(Calendar.YEAR),
						calendar.get(Calendar.MONTH) + 1,
						calendar.get(Calendar.DAY_OF_MONTH),
						calendar.get(Calendar.HOUR_OF_DAY),
						calendar.get(Calendar.MINUTE),
						calendar.get(Calendar.SECOND),
						calendar.get(Calendar.MILLISECOND))));
	}
	public static void print(long timeMillis, String messageFormat) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		print(calendar, messageFormat);
	}
	public static void setToBeginning(Calendar calendar, int field) {
		switch (field) {
			case Calendar.YEAR:
				calendar.set(Calendar.MONTH, 0);
			case Calendar.MONTH:
				calendar.set(Calendar.DAY_OF_MONTH, 1);
			case Calendar.DAY_OF_MONTH:
			case Calendar.DAY_OF_WEEK:
			case Calendar.DAY_OF_WEEK_IN_MONTH:
			case Calendar.DAY_OF_YEAR:
				calendar.set(Calendar.HOUR_OF_DAY, 0);
			case Calendar.HOUR:
			case Calendar.HOUR_OF_DAY:
				calendar.set(Calendar.MINUTE, 0);
			case Calendar.MINUTE:
				calendar.set(Calendar.SECOND, 0);
			case Calendar.SECOND:
				calendar.set(Calendar.MILLISECOND, 0);
		}
	}
	public static long setToBeginning(long timeMillis, int field) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		setToBeginning(calendar, field);
		return calendar.getTimeInMillis();
	}
	public static long addTo(long timeMillis, int field, int amount) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		calendar.add(field, amount);
		return calendar.getTimeInMillis();
	}
	public static void setToEnd(Calendar calendar, int field) {
		setToBeginning(calendar, field);
		calendar.add(field, 1);
	}
	public static long setToEnd(long timeMillis, int field) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		setToEnd(calendar, field);
		return calendar.getTimeInMillis();
	}
}
