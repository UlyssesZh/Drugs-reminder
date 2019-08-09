package ulysses.apps.drugsreminder.libraries;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import ulysses.apps.drugsreminder.elements.Reminder;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.receivers.AlarmReceiver;
import ulysses.apps.drugsreminder.receivers.NotificationReceiver;

public final class AlarmsLibrary {
	private static SparseArray<List<PendingIntent>> alarmIntents = new SparseArray<List<PendingIntent>>();
	private static SparseArray<List<PendingIntent>> notificationIntents = new SparseArray<List<PendingIntent>>();
	/** Set up the time-scheduled alarms and notifications about one specified reminder.
	 * @param context the context from which the {@link AlarmManager} will be gotten.
	 * @param reminderID specifying the reminder whose alarms will be set up.*/
	public static void setupAlarms(@NotNull Context context, int reminderID) {
		clearIntents(alarmIntents, context, reminderID);
		clearIntents(notificationIntents, context, reminderID);
		if (!ElementsLibrary.doesNotHaveReminder(reminderID)) {
			Reminder reminder = ElementsLibrary.findReminderByID(reminderID);
			if (reminder.isEnabled()) {
				AlarmManager alarmManager =
						(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				if (alarmManager == null) return;
				List<PendingIntent> alarmIntentsList = alarmIntents.get(reminderID);
				List<PendingIntent> notificationIntentsList = notificationIntents.get(reminderID);
				long intervalMillis = 86400000 * reminder.getRepeatPeriod();
				List<Long> triggerAtMillis = triggerAtMillis(reminder.alarmTimeMillis(),
						intervalMillis);
				if (reminder.isDelayed()) {
					// modify triggerAtMillis, set a delayed alarm
					PendingIntent alarmIntent = generateAlarmIntent(context, reminderID,
							true);
					alarmManager.setExact(AlarmManager.RTC_WAKEUP,
							delayedTriggerAtMillis(triggerAtMillis, intervalMillis), alarmIntent);
				}
				// set un-delayed alarms
				for (long millis : triggerAtMillis) {
					PendingIntent alarmIntent = generateAlarmIntent(context, reminderID,
							false);
					alarmIntentsList.add(alarmIntent);
					alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, millis,
							intervalMillis, alarmIntent);
				}
				if (!Preferences.reminderAdvanceTime.isZero())
					// set notifications
					for (long millis : triggerAtMillis) {
						millis -= Preferences.reminderAdvanceTime.millis();
						if (millis < System.currentTimeMillis()) millis += intervalMillis;
						PendingIntent notificationIntent = generateNotificationIntent(context,
								reminderID);
						notificationIntentsList.add(notificationIntent);
						alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, millis,
								intervalMillis, notificationIntent);
					}
			}
		}
	}
	/** @param alarmTimeMillis should be obtained from {@link Reminder#alarmTimeMillis()}.
	 * @param intervalMillis the repeat period of the alarms represented in millis.
	 * @return a list of wall time millis of the reminder's next triggered time, each of which
	 * corresponds to one of the reminder's meal.*/
	private static List<Long> triggerAtMillis(@NotNull List<Long> alarmTimeMillis,
	                                                      long intervalMillis) {
		List<Long> result = new ArrayList<Long>(alarmTimeMillis.size());
		long currentMillis = System.currentTimeMillis();
		for (long millis : alarmTimeMillis) {
			if (currentMillis > millis)
				millis += ((currentMillis - millis) / intervalMillis + 1) * intervalMillis;
			result.add(millis);
		}
		return result;
	}
	/** Add the min time by intervalMillis, and return the min time plus the delayed time.
	 * @param triggerAtMillis WILL be modified after invoking the method.
	 * @param intervalMillis will be added to the min value in triggerAtMillis.
	 * @return the min value in triggerAtMillis plus {@link Preferences#delayTime} in millis.*/
	private static long delayedTriggerAtMillis(@NotNull List<Long> triggerAtMillis,
	                                           long intervalMillis) {
		long min = Long.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < triggerAtMillis.size(); i++) {
			long millis = triggerAtMillis.get(i);
			if (millis < min) {
				min = millis;
				index = i;
			}
		}
		triggerAtMillis.set(index, min + intervalMillis);
		return min + Preferences.delayTime.millis();
	}
	/** Clear intents.get(reminderID). If the reminder is not enabled, make it null; otherwise make
	 * it with a new ArrayList.*/
	private static void clearIntents(@NotNull SparseArray<List<PendingIntent>> intents,
	                                 @NotNull Context context, int reminderID) {
		List<PendingIntent> intentsList = intents.get(reminderID);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (intentsList != null && alarmManager != null)
			for (PendingIntent alarmIntent : intentsList) alarmManager.cancel(alarmIntent);
		intents.put(reminderID, null);
		if (!ElementsLibrary.doesNotHaveReminder(reminderID)) {
			Reminder reminder = ElementsLibrary.findReminderByID(reminderID);
			if (reminder.isEnabled())
				intents.put(reminderID, new ArrayList<PendingIntent>(reminder.getMealIDs().size()));
		}
	}
	/** Invoke {@link #setupAlarms(Context, int)} for each reminder.*/
	public static void setupAllAlarms(@NotNull Context context) {
		for (int ID = 0; !ElementsLibrary.reminderIDOutOfBound(ID); ID++)
			setupAlarms(context, ID);
	}
	/** @return an PendingIntent fired as an alarm.*/
	private static PendingIntent generateAlarmIntent(Context context, int reminderID,
	                                                 boolean clearDelay) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra("reminderID", reminderID);
		intent.putExtra("clearDelay", clearDelay);
		return PendingIntent.getBroadcast(context, 0x0520, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}
	/** @return a PendingIntent fired as a notification.*/
	private static PendingIntent generateNotificationIntent(Context context, int reminderID) {
		Intent intent = new Intent(context, NotificationReceiver.class);
		intent.putExtra("reminderID", reminderID);
		return PendingIntent.getBroadcast(context, 0x0520, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
