package ulysses.apps.drugsreminder.util;

import android.content.res.Resources;
import androidx.annotation.NonNull;
import android.widget.TimePicker;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Calendar;

import ulysses.apps.drugsreminder.R;

public class Time implements Serializable, Comparable<Time> {
	protected int hour = 0;
	protected int minute = 0;
	public Time(int hour, int minute) {
		set(hour, minute);
	}
	public Time() {}
	public Time(@NotNull TimePicker timePicker) {
		set(timePicker.getHour(), timePicker.getMinute());
	}
	public Time(@NotNull Calendar calendar) {
		set(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
	}
	public Time(long wallTimeMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(wallTimeMillis);
		set(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
	}
	public Time(int minutes) {
		set(minutes / 60, minutes % 60);
	}
	public int getHour() {
		return hour;
	}
	public void setHour(int hours) {
		this.hour = clamp(hours, 24);
	}
	public int getMinute() {
		return minute;
	}
	public void setMinute(int minutes) {
		this.minute = clamp(minutes, 60);
	}
	public void set(int hours, int minutes) {
		setHour(hours);
		setMinute(minutes);
	}
	@Contract(" -> new")
	@NotNull
	public static Time now() {
		return new Time(Calendar.getInstance());
	}
	@Contract(pure = true)
	private static int clamp(int n, int min, int max) {
		return n < min ? min : n > max ? max : n;
	}
	@Contract(pure = true)
	private static int clamp(int n, int bound) {
		return clamp(n, 0, bound - 1);
	}
	@Override
	@NonNull
	public String toString() {
		return String.format("%02d:%02d", hour, minute);
	}
	public String toString(Resources resources) {
		if (hour == 0) return resources.getString(R.string.time_format_without_hour, minute);
		else return resources.getString(R.string.time_format, hour, minute);
	}
	public Time plus(Time other) {
		int resultMinute = minute + other.getMinute();
		int resultHour = hour + other.getHour();
		if (resultMinute >= 60) {
			resultMinute -= 60;
			resultHour += 1;
		}
		if (resultHour >= 24) resultHour -= 24;
		return new Time(resultHour, resultMinute);
	}
	public Time minus(Time other) {
		int resultMinute = minute - other.minute;
		int resultHour = hour - other.hour;
		if (resultMinute < 0) {
			resultMinute += 60;
			resultHour -= 1;
		}
		if (resultHour < 0) resultHour += 24;
		return new Time(resultHour, resultMinute);
	}
	@Contract(value = "null -> false", pure = true)
	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Time)) return false;
		Time other = (Time) obj;
		return hour == other.hour && minute == other.minute;
	}
	@Contract(pure = true)
	public static boolean sumOverOneDay(@NotNull Time time1, @NotNull Time time2) {
		int hour = time1.hour + time2.hour;
		if (time1.minute + time2.minute >= 60) hour += 1;
		return hour >= 24;
	}
	public long millis() {
		return hour * 3600000 + minute * 60000;
	}
	public int minutes() {
		return hour * 60 + minute;
	}
	public boolean isZero() {
		return hour == 0 && minute == 0;
	}
	@Override
	public int compareTo(@NonNull Time o) {
		if (hour != o.hour) return hour > o.hour ? 1 : -1;
		return Integer.compare(minute, o.minute);
	}
}
