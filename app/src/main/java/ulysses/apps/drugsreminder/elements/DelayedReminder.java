package ulysses.apps.drugsreminder.elements;

import android.content.res.Resources;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.util.CalendarUtils;
import ulysses.apps.drugsreminder.util.Time;

public class DelayedReminder implements IReminder {
	private int ID;
	protected Reminder reminder;
	protected long triggerAtMillis;
	public DelayedReminder(int reminderID, long triggerAtMillis) {
		this.ID = ElementsLibrary.remindersNumber();
		this.reminder = (Reminder) ElementsLibrary.findReminderByID(reminderID);
		this.triggerAtMillis = triggerAtMillis;
	}
	public DelayedReminder(int ID, int reminderID, long triggerAtMillis) {
		this.ID = ID;
		this.reminder = (Reminder) ElementsLibrary.findReminderByID(reminderID);
		this.triggerAtMillis = triggerAtMillis;
	}
	@Override
	public boolean isRepeating() {
		return false;
	}
	@Override
	public int getID() {
		return ID;
	}
	@Override
	public List<Integer> getInvolvingReminderIDs() {
		return new ArrayList<Integer>(0);
	}
	@Override
	public List<Integer> getDrugIDs() {
		return reminder.getDrugIDs();
	}
	@Override
	public List<String> getUsageDosages() {
		return reminder.getUsageDosages();
	}
	@Override
	public boolean isEnabled() {
		return true;
	}
	public int getReminderID() {
		return reminder.getID();
	}
	public Reminder getReminder() {
		return reminder;
	}
	@Override
	public List<Long> alarmTimeMillis() {
		List<Long> result = new ArrayList<Long>(1);
		result.add(triggerAtMillis);
		return result;
	}
	@Override
	public String nextTimeString(Resources resources) {
		long currentTimeMillis = System.currentTimeMillis();
		int pendingDaysNumber = (int) (
				(CalendarUtils.setToBeginning(triggerAtMillis, Calendar.DAY_OF_MONTH) -
						 CalendarUtils.setToBeginning(currentTimeMillis, Calendar.DAY_OF_MONTH)) /
						86400000);
		int timeDifference = (int) ((triggerAtMillis - currentTimeMillis) / 60000) + 1;
		switch (Integer.compare(pendingDaysNumber, 1)) {
			case 1:
				return resources.getString(R.string.day_format, pendingDaysNumber);
			case -1:
				return new Time(timeDifference).toString(resources);
			default:
				return timeDifference > 86400000 ?  resources.getString(R.string.day_format, 1) :
							   new Time(timeDifference).toString(resources);
		}
	}
	public long getTriggerAtMillis() {
		return triggerAtMillis;
	}
	public void setTriggerAtMillis(long triggerAtMillis) {
		this.triggerAtMillis = triggerAtMillis;
	}
	@Override
	public int getRepeatPeriod() {
		return Integer.MAX_VALUE;
	}
	@Override
	public String timeString(Resources resources) {
		return resources.getString(R.string.temporary_hint);
	}
	@Override
	public String drugsString(Resources resources) {
		return reminder.drugsString(resources);
	}
}
