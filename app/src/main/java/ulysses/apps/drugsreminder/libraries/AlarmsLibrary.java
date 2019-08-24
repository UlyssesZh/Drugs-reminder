package ulysses.apps.drugsreminder.libraries;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import ulysses.apps.drugsreminder.elements.IReminder;
import ulysses.apps.drugsreminder.elements.Reminder;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.receivers.AlarmReceiver;
import ulysses.apps.drugsreminder.receivers.NotificationReceiver;
import ulysses.apps.drugsreminder.util.BackgroundThread;
import ulysses.apps.drugsreminder.util.CalendarUtils;

public final class AlarmsLibrary {
	private static SparseArray<List<PendingIntent>> alarmIntents = new SparseArray<List<PendingIntent>>();
	private static SparseArray<List<PendingIntent>> notificationIntents = new SparseArray<List<PendingIntent>>();
	/** Set up the time-scheduled alarms and notifications about a specified reminder.
	 * @param context the context from which the pending intents will start.
	 * @param reminderID specifying the reminder whose alarms will be setRepeating up.
	 * @return whether the list of reminders need to refresh after calling the method.*/
	private static boolean setupAlarms(@NotNull Context context, int reminderID) {
		clearAlarms(context, reminderID);
		IReminder reminder = ElementsLibrary.findReminderByID(reminderID);
		if (!reminder.isEnabled()) return false;
		boolean result = false;
		List<PendingIntent> alarmIntentsList = alarmIntents.get(reminderID);
		List<PendingIntent> notificationIntentsList = notificationIntents.get(reminderID);
		String head = "reminder" + reminderID;
		if (reminder.isRepeating()) { // set un-delayed reminder
			long intervalMillis = 86400000 * reminder.getRepeatPeriod();
			List<Long> triggerAtMillis = reminder.alarmTimeMillis();
			// set repeating alarms
			for (int i = 0; i < triggerAtMillis.size(); i++) {
				long millis = triggerAtMillis.get(i);
				PendingIntent alarmIntent = generateAlarmPendingIntent(context, reminderID, millis);
				alarmIntentsList.add(alarmIntent);
				setRepeating(millis, intervalMillis, alarmIntent,
						head + "un-delayed" + i);
			}
			// set notifications
			if (!Preferences.reminderAdvanceTime.isZero())
				for (int i = 0; i < triggerAtMillis.size(); i++) {
					long millis = triggerAtMillis.get(i) - Preferences.reminderAdvanceTime.millis();
					PendingIntent notificationIntent =
							generateNotificationPendingIntent(context, reminderID);
					notificationIntentsList.add(notificationIntent);
					setRepeating(millis, intervalMillis, notificationIntent,
							head + "notification" + i);
				}
		} else { // set delayed reminders
			List<Long> triggerAtMillis = reminder.alarmTimeMillis();
			for (int i = 0; i < triggerAtMillis.size(); i++) {
				long millis = triggerAtMillis.get(i);
				if (millis < System.currentTimeMillis() - 60000) {
					ElementsLibrary.deleteReminder(reminderID);
					result = true;
					break;
				}
				PendingIntent alarmIntent = generateAlarmPendingIntent(context, reminderID, millis);
				alarmIntentsList.add(alarmIntent);
				set(millis, alarmIntent, head + "delayed" + i);
			}
		}
		BackgroundThread.start();
		return result;
	}
	/** Set a non-repeating alarm.*/
	private static void set(long triggerAtMillis, PendingIntent pendingIntent, String taskName) {
		BackgroundThread.putTask(taskName, () -> {
			CalendarUtils.logMessage(triggerAtMillis, taskName + " triggers at %s.");
			if (triggerAtMillis == BackgroundThread.getStartTimeMillis())
				sendPendingIntent(pendingIntent);
		});
	}
	/** Set a repeating alarm.*/
	private static void setRepeating(long triggerAtMillis, long intervalMillis,
	                                 PendingIntent pendingIntent, String taskName) {
		BackgroundThread.putTask(taskName, () -> {
			CalendarUtils.logMessage(triggerAtMillis, taskName + " triggers at %s.");
			long timeDifference = BackgroundThread.getStartTimeMillis() - triggerAtMillis;
			if (timeDifference >= 0 && timeDifference % intervalMillis == 0)
				sendPendingIntent(pendingIntent);
		});
	}
	/** Clear intents.get(reminderID). If the reminder is not enabled, make it null; otherwise make
	 * it with a new ArrayList.*/
	private static void clearIntents(@NotNull SparseArray<List<PendingIntent>> intents,
	                                 @NotNull Context context, int reminderID) {
		List<PendingIntent> intentsList = intents.get(reminderID);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (intentsList != null && alarmManager != null)
			for (PendingIntent alarmIntent : intentsList) {
				alarmIntent.cancel();
				alarmManager.cancel(alarmIntent);
			}
		intents.put(reminderID, null);
		if (!ElementsLibrary.doesNotHaveReminder(reminderID)) {
			IReminder reminder = ElementsLibrary.findReminderByID(reminderID);
			if (reminder.isEnabled())
				intents.put(reminderID, new ArrayList<PendingIntent>(
						reminder.isRepeating() ? ((Reminder) reminder).getMealIDs().size() : 1));
		}
	}
	/** Clear the alarms of a specified reminder.*/
	private static void clearAlarms(Context context, int reminderID) {
		clearIntents(alarmIntents, context, reminderID);
		clearIntents(notificationIntents, context, reminderID);
		if (BackgroundThread.isAlive())
			BackgroundThread.interrupt();
		BackgroundThread.clearTasks("reminder" + reminderID + ".*");
	}
	/** Invoke {@link #setupAlarms(Context, int)} for each reminder.
	 * @return whether the list of reminders need to refresh after calling the method.*/
	public static boolean setupAllAlarms(@NotNull Context context) {
		boolean result = false;
		for (int ID = 0; !ElementsLibrary.reminderIDOutOfBound(ID); ID++)
			if (!ElementsLibrary.doesNotHaveReminder(ID) && setupAlarms(context, ID))
				result = true;
		return result;
	}
	/** @return a PendingIntent referring to {@link AlarmReceiver}.*/
	private static PendingIntent generateAlarmPendingIntent(Context context, int reminderID,
	                                                        long triggerAtMillis) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra("reminderID", reminderID);
		intent.putExtra("triggerAtMillis", triggerAtMillis);
		return PendingIntent.getBroadcast(context, 0x0520, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}
	/** @return a PendingIntent referring to {@link NotificationReceiver}.*/
	private static PendingIntent generateNotificationPendingIntent(Context context, int reminderID) {
		Intent intent = new Intent(context, NotificationReceiver.class);
		intent.putExtra("reminderID", reminderID);
		intent.putExtra("forRemindingAdvance", true);
		return PendingIntent.getBroadcast(context, 0x0520, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}
	/** Equivalent to {@link PendingIntent#send()} except that it will catch the
	 * {@link android.app.PendingIntent.CanceledException}.*/
	private static void sendPendingIntent(@NotNull PendingIntent pendingIntent) {
		try {
			pendingIntent.send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
	}
}
