package ulysses.apps.drugsreminder.elements;

import android.content.res.Resources;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.util.CalendarUtils;
import ulysses.apps.drugsreminder.util.Time;

public class Reminder implements IReminder {
	private int ID;
	protected List<Integer> mealIDs;
	protected Time relativeTime;
	protected List<Integer> drugIDs;
	protected List<String> usageDosages;
	protected boolean before;
	protected boolean enabled;
	protected int repeatPeriod;
	protected long createdTime;
	//protected int delayed;
	public Reminder(int ID, List<Integer> mealIDs, boolean before, Time relativeTime,
	                List<Integer> drugIDs, List<String> usageDosages,  int repeatPeriod,
	                long createdTime) {
		this.ID = ID;
		this.mealIDs = mealIDs;
		this.before = before;
		this.relativeTime = relativeTime;
		this.drugIDs = drugIDs;
		this.usageDosages = usageDosages;
		this.repeatPeriod = repeatPeriod;
		this.createdTime = createdTime;
		enabled = true;
		//delayed = 0;
	}
	@Override
	public int getID() {
		return ID;
	}
	@Override
	public List<Integer> getDrugIDs() {
		return drugIDs;
	}
	public void setDrugIDs(List<Integer> drugIDs) {
		this.drugIDs = drugIDs;
	}
	public List<Integer> getMealIDs() {
		return mealIDs;
	}
	public void setMealIDs(List<Integer> mealIDs) {
		this.mealIDs = mealIDs;
	}
	@Override
	public List<String> getUsageDosages() {
		return usageDosages;
	}
	public void setUsageDosages(List<String> usageDosages) {
		this.usageDosages = usageDosages;
	}
	public void setRelativeTime(Time relativeTime) {
		this.relativeTime = relativeTime;
	}
	public Time getRelativeTime() {
		return relativeTime;
	}
	public boolean isBefore() {
		return before;
	}
	public void setBefore(boolean before) {
		this.before = before;
	}
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (Preferences.resetStarting &&
				    !Preferences.startingTimeType.equals(Preferences.STARTING_TIME_TYPE_PICK))
			setCreatedTime(System.currentTimeMillis());
	}
	@Override
	public int getRepeatPeriod() {
		return repeatPeriod;
	}
	public void setRepeatPeriod(int repeatPeriod) {
		this.repeatPeriod = repeatPeriod;
	}
	public long getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}
	@Override
	public String timeString(Resources resources) {
		return Reminder.timeString(mealIDs, relativeTime, before, resources);
	}
	/** @return how much time it will pass before the next alarm comes.*/
	@NotNull
	private Time nextTime() {
		return nextTime(Time.now());
	}
	/** @param now the time which would be regarded as 'now'.
	 * @return the same as {@link #nextTime()}, except that it regard 'now' as the param you gave
	 * it.*/
	@NotNull
	private Time nextTime(Time now) {
		List<Time> remindTimes = new ArrayList<Time>(mealIDs.size());
		if (before)
			for (int mealID : mealIDs)
				remindTimes.add(ElementsLibrary.findMealByID(mealID).getTime()
						                .minus(relativeTime).minus(now));
		else
			for (int mealID : mealIDs)
				remindTimes.add(ElementsLibrary.findMealByID(mealID).getTime()
						                .plus(relativeTime).minus(now));
		return Collections.min(remindTimes);
	}
	/** @param currentTimeMillis the time passed represented in millis passed since 1970, which
	 *                            would be regarded as 'now'.
	 * @return the same as {@link #pendingDaysNumber()}, except that it regard 'now' as the param
	 * you gave it.*/
	private int pendingDaysNumber(long currentTimeMillis) {
		Calendar startingDayCalendar = startingTimeCalendar();
		Time startingTime = new Time(startingDayCalendar);
		if (Time.sumOverOneDay(startingTime, nextTime(startingTime)))
			startingDayCalendar.add(Calendar.DAY_OF_MONTH, 1);
		Calendar currentDayCalendar = Calendar.getInstance();
		currentDayCalendar.setTimeInMillis(currentTimeMillis);
		CalendarUtils.setToBeginning(startingDayCalendar, Calendar.DAY_OF_MONTH);
		CalendarUtils.setToBeginning(currentDayCalendar, Calendar.DAY_OF_MONTH);
		long startingDayMillis = startingDayCalendar.getTimeInMillis();
		long currentDayMillis = currentDayCalendar.getTimeInMillis();
		Time now = new Time(currentTimeMillis);
		boolean overDay = Time.sumOverOneDay(now, nextTime(now));
		int result;
		switch (Long.compare(currentDayMillis, startingDayMillis)) {
			case 1:
				result = (int) ((currentTimeMillis - startingDayMillis) / 86400000) % repeatPeriod;
				result = repeatPeriod - result;
				if (result == repeatPeriod && !overDay) result = 0;
				break;
			case -1:
				result = (int) ((startingDayMillis - currentTimeMillis) / 86400000);
				break;
			default:
				result = overDay ? repeatPeriod : 0;
				break;
		}
		if (result == 1 && overDay) result = 0;
		return result;
	}
	/** @return how many days will pass before the next alarm time. */
	private int pendingDaysNumber() {
		return pendingDaysNumber(System.currentTimeMillis());
	}
	/** @return a List containing time represented in millis passed since 1970 of when the reminder
	 * would alarm on the first day. */
	@Override
	public List<Long> alarmTimeMillis() {
		Calendar calendar = startingTimeCalendar();
		Time startingTime = new Time(calendar);
		if (Time.sumOverOneDay(startingTime, nextTime(startingTime)))
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		CalendarUtils.setToBeginning(calendar, Calendar.MINUTE);
		List<Long> result = new ArrayList<Long>(mealIDs.size());
		long relativeTimeMillis = relativeTime.millis();
		if (before) relativeTimeMillis *= -1;
		for (int mealID : mealIDs) {
			Time mealTime = ElementsLibrary.findMealByID(mealID).getTime();
			calendar.set(Calendar.HOUR_OF_DAY, mealTime.getHour());
			calendar.set(Calendar.MINUTE, mealTime.getMinute());
			result.add(calendar.getTimeInMillis() + relativeTimeMillis);
		}
		return result;
	}
	private Calendar startingTimeCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(createdTime);
		if (Preferences.startingTimeType.equals(Preferences.STARTING_TIME_TYPE_NEXT)) return calendar;
		CalendarUtils.setToBeginning(calendar, Calendar.DAY_OF_MONTH);
		return calendar;
	}
	/** @return the String representing how soon the next alarm time is coming. When it is greater
	 * than one day, it is represented in the format of %dd, and %dh%dm otherwise. */
	@Override
	public String nextTimeString(Resources resources) {
		int pendingDaysNumber = pendingDaysNumber();
		if (pendingDaysNumber > 0)
			return resources.getString(R.string.day_format, pendingDaysNumber);
		else
			return nextTime().toString(resources);
	}
	/** @return a String representing the reminder's time, e.g. 30m before lunch. */
	@NotNull
	public static String timeString(@NotNull List<Integer> mealIDs, Time relativeTime,
	                                boolean before, Resources resources) {
		StringBuilder mealNamesBuilder = new StringBuilder();
		int iMax = mealIDs.size() - 1;
		for (int i = 0; i <= iMax; i++) {
			int mealID = mealIDs.get(i);
			mealNamesBuilder.append(ElementsLibrary.findMealByID(mealID).getName());
			if (i != iMax)
				mealNamesBuilder.append(resources.getString(R.string.comma));
		}
		return resources.getString(R.string.reminder_time_format, relativeTime.toString(resources),
				resources.getString(before ? R.string.before : R.string.after),
				mealNamesBuilder.toString());
	}
	@Override
	public String drugsString(Resources resources) {
		StringBuilder builder = new StringBuilder();
		int iMax = drugIDs.size() - 1;
		for (int i = 0; i <= iMax; i++) {
			builder.append(ElementsLibrary.findDrugByID(drugIDs.get(i)).getName());
			if (i != iMax) builder.append(resources.getString(R.string.comma));
		}
		return builder.toString();
	}
	@Override
	public List<Integer> getInvolvingReminderIDs() {
		return new ArrayList<Integer>(0);
	}
	@Override
	public boolean isRepeating() {
		return true;
	}
}
