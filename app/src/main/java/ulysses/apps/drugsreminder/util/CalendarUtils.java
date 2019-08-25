package ulysses.apps.drugsreminder.util;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.Calendar;

public final class CalendarUtils {
	public static final String DEFAULT_FORMAT = "%04d-%02d-%02d %02d:%02d:%02d.%03d";
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
	@NotNull
	public static String format(@NotNull Calendar calendar, String format) {
		return String.format(format,
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE),
				calendar.get(Calendar.SECOND),
				calendar.get(Calendar.MILLISECOND));
	}
	@NotNull
	public static String format(@NotNull Calendar calendar) {
		return format(calendar, DEFAULT_FORMAT);
	}
	@NotNull
	public static String format(long timeMillis, String format) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		return format(calendar, format);
	}
	@NotNull
	public static String format(long timeMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		return format(calendar);
	}
	public static void log(Calendar calendar, String format) {
		LogUtils.d("CalendarUtils", format(calendar, format));
	}
	public static void log(Calendar calendar) {
		LogUtils.d("CalendarUtils", format(calendar));
	}
	public static void log(long timeMillis, String format) {
		LogUtils.d("CalendarUtils", format(timeMillis, format));
	}
	public static void log(long timeMillis) {
		LogUtils.d("CalendarUtils", format(timeMillis));
	}
	public static void logMessage(Calendar calendar, String format, String messageFormat) {
		LogUtils.d("CalendarUtils", String.format(messageFormat, format(calendar, format)));
	}
	public static void logMessage(Calendar calendar, String messageFormat) {
		LogUtils.d("CalendarUtils", String.format(messageFormat, format(calendar)));
	}
	public static void logMessage(long timeMillis, String format, String messageFormat) {
		LogUtils.d("CalendarUtils", String.format(messageFormat, format(timeMillis, format)));
	}
	public static void logMessage(long timeMillis, String messageFormat) {
		LogUtils.d("CalendarUtils", String.format(messageFormat, format(timeMillis)));
	}
	public static void printlnTo(@NotNull PrintStream printStream, Calendar calendar, String format) {
		printStream.println(format(calendar, format));
	}
	public static void printlnTo(@NotNull PrintStream printStream, Calendar calendar) {
		printStream.println(format(calendar));
	}
	public static void printlnTo(@NotNull PrintStream printStream, long timeMillis, String format) {
		printStream.println(format(timeMillis, format));
	}
	public static void printlnTo(@NotNull PrintStream printStream, long timeMillis) {
		printStream.println(format(timeMillis));
	}
	public static void printlnTo(Calendar calendar, String format) {
		printlnTo(System.out, calendar, format);
	}
	public static void printlnTo(Calendar calendar) {
		printlnTo(System.out, calendar);
	}
	public static void printlnTo(long timeMillis, String format) {
		printlnTo(System.out, timeMillis, format);
	}
	public static void printlnTo(long timeMillis) {
		printlnTo(System.out, timeMillis);
	}
	public static void printTo(@NotNull PrintStream printStream, Calendar calendar, String format) {
		printStream.print(format(calendar, format));
	}
	public static void printTo(@NotNull PrintStream printStream, Calendar calendar) {
		printStream.print(format(calendar));
	}
	public static void printTo(@NotNull PrintStream printStream, long timeMillis, String format) {
		printStream.print(format(timeMillis, format));
	}
	public static void printTo(@NotNull PrintStream printStream, long timeMillis) {
		printStream.print(format(timeMillis));
	}
	public static void printTo(Calendar calendar, String format) {
		printTo(System.out, calendar, format);
	}
	public static void printTo(Calendar calendar) {
		printTo(System.out, calendar);
	}
	public static void printTo(long timeMillis, String format) {
		printTo(System.out, timeMillis, format);
	}
	public static void printTo(long timeMillis) {
		printTo(System.out, timeMillis);
	}
	public static void printlnMessageTo(@NotNull PrintStream printStream, Calendar calendar, String format, String messageFormat) {
		printStream.println(String.format(messageFormat, format(calendar, format)));
	}
	public static void printlnMessageTo(@NotNull PrintStream printStream, Calendar calendar, String messageFormat) {
		printStream.println(String.format(messageFormat, format(calendar)));
	}
	public static void printlnMessageTo(@NotNull PrintStream printStream, long timeMillis, String format, String messageFormat) {
		printStream.println(String.format(messageFormat, format(timeMillis, format)));
	}
	public static void printlnMessageTo(@NotNull PrintStream printStream, long timeMillis, String messageFormat) {
		printStream.println(String.format(messageFormat, format(timeMillis)));
	}
	public static void printlnMessageTo(Calendar calendar, String format, String messageFormat) {
		printlnMessageTo(System.out, calendar, format);
	}
	public static void printlnMessageTo(Calendar calendar, String messageFormat) {
		printlnMessageTo(System.out, calendar, messageFormat);
	}
	public static void printlnMessageTo(long timeMillis, String format, String messageFormat) {
		printlnMessageTo(System.out, timeMillis, format);
	}
	public static void printlnMessageTo(long timeMillis, String messageFormat) {
		printlnMessageTo(System.out, timeMillis, messageFormat);
	}
	public static void printMessageTo(@NotNull PrintStream printStream, Calendar calendar, String format, String messageFormat) {
		printStream.print(String.format(messageFormat, format(calendar, format)));
	}
	public static void printMessageTo(@NotNull PrintStream printStream, Calendar calendar, String messageFormat) {
		printStream.print(String.format(messageFormat, format(calendar)));
	}
	public static void printMessageTo(@NotNull PrintStream printStream, long timeMillis, String format, String messageFormat) {
		printStream.print(String.format(messageFormat, format(timeMillis, format)));
	}
	public static void printMessageTo(@NotNull PrintStream printStream, long timeMillis, String messageFormat) {
		printStream.print(String.format(messageFormat, format(timeMillis)));
	}
	public static void printMessageTo(Calendar calendar, String format, String messageFormat) {
		printMessageTo(System.out, calendar, format);
	}
	public static void printMessageTo(Calendar calendar, String messageFormat) {
		printMessageTo(System.out, calendar, messageFormat);
	}
	public static void printMessageTo(long timeMillis, String format, String messageFormat) {
		printMessageTo(System.out, timeMillis, format);
	}
	public static void printMessageTo(long timeMillis, String messageFormat) {
		printMessageTo(System.out, timeMillis, messageFormat);
	}
}
