package ulysses.apps.drugsreminder.libraries;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
	 * @param reminderID specifying the reminder whose alarms will be setRepeating up.*/
	private static void setupAlarms(@NotNull Context context, int reminderID) {
		clearAlarms(context, reminderID);
		Reminder reminder = ElementsLibrary.findReminderByID(reminderID);
		if (!reminder.isEnabled()) return;
		List<PendingIntent> alarmIntentsList = alarmIntents.get(reminderID);
		List<PendingIntent> notificationIntentsList = notificationIntents.get(reminderID);
		long intervalMillis = 86400000 * reminder.getRepeatPeriod();
		List<Long> triggerAtMillis = triggerAtMillis(reminder.alarmTimeMillis(), intervalMillis);
		String head = "reminder" + reminderID;
		// set un-delayed alarms
		for (int i = 0; i < triggerAtMillis.size(); i++) {
			PendingIntent alarmIntent = generateAlarmPendingIntent(context, reminderID, i);
			alarmIntentsList.add(alarmIntent);
			setRepeating(triggerAtMillis.get(i), intervalMillis, alarmIntent,
					head + "un-delayed" + i);
		}
		if (Preferences.reminderAdvanceTime.isZero()) return;
		// set notifications
		for (int i = 0; i < triggerAtMillis.size(); i++) {
			long millis = triggerAtMillis.get(i) - Preferences.reminderAdvanceTime.millis();
			if (millis < System.currentTimeMillis()) millis += intervalMillis;
			PendingIntent notificationIntent = generateNotificationPendingIntent(context, reminderID);
			notificationIntentsList.add(notificationIntent);
			setRepeating(millis, intervalMillis, notificationIntent,
					head + "notification" + i);
		}
		BackgroundThread.start();
	}
	private static void setRepeating(long triggerAtMillis, long intervalMillis,
	                                 PendingIntent pendingIntent, String taskName) {
		BackgroundThread.putTask(taskName, () -> {
			CalendarUtils.print(triggerAtMillis, "Will " + taskName + " trigger at %s?");
			long timeDifference = BackgroundThread.getStartTimeMillis() - triggerAtMillis;
			if (timeDifference >= 0 && timeDifference % intervalMillis == 0)
				sendPendingIntent(pendingIntent);
		});
	}
	/** @param alarmTimeMillis should be obtained from {@link Reminder#alarmTimeMillis()}.
	 * @param intervalMillis the repeat period of the alarms represented in millis.
	 * @return a list of wall time millis of the reminder's next triggered time, each of which
	 * corresponds to one of the reminder's meal.*/
	private static List<Long> triggerAtMillis(@NotNull List<Long> alarmTimeMillis,
	                                          long intervalMillis) {
		List<Long> result = new ArrayList<Long>(alarmTimeMillis.size());
		long currentMillis = System.currentTimeMillis();
		for (int i = 0; i < alarmTimeMillis.size(); i++) {
			long millis = alarmTimeMillis.get(i);
			if (currentMillis > millis)
				millis += ((currentMillis - millis) / intervalMillis + 1) * intervalMillis;
			result.add(millis);
		}
		return result;
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
			Reminder reminder = ElementsLibrary.findReminderByID(reminderID);
			if (reminder.isEnabled())
				intents.put(reminderID, new ArrayList<PendingIntent>(reminder.getMealIDs().size()));
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
	/** Invoke {@link #setupAlarms(Context, int)} for each reminder.*/
	public static void setupAllAlarms(@NotNull Context context) {
		for (int ID = 0; !ElementsLibrary.reminderIDOutOfBound(ID); ID++)
			setupAlarms(context, ID);
	}
	/** @return a PendingIntent referring to {@link AlarmReceiver}.*/
	private static PendingIntent generateAlarmPendingIntent(Context context, int reminderID,
	                                                        int mealIDIndex/*, boolean clearDelay*/) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra("reminderID", reminderID);
		intent.putExtra("mealIDIndex", mealIDIndex);
		/*intent.putExtra("clearDelay", clearDelay);*/
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
