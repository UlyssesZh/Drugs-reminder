package ulysses.apps.drugsreminder.libraries;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ulysses.apps.drugsreminder.activities.AlarmActivity;
import ulysses.apps.drugsreminder.elements.Reminder;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.receivers.AlarmReceiver;
import ulysses.apps.drugsreminder.receivers.NotificationReceiver;
import ulysses.apps.drugsreminder.services.NotificationService;
import ulysses.apps.drugsreminder.util.BackgroundThread;
import ulysses.apps.drugsreminder.util.CalendarUtils;

public final class AlarmsLibrary {
	private static SparseArray<List<PendingIntent>> alarmIntents = new SparseArray<List<PendingIntent>>();
	private static SparseArray<List<PendingIntent>> notificationIntents = new SparseArray<List<PendingIntent>>();
	/** Set up the time-scheduled alarms and notifications about a specified reminder.
	 * @param context the context from which the pending intents will start.
	 * @param reminderID specifying the reminder whose alarms will be setRepeating up.
	 * @param listener the listener that will trigger just after invoking
	 *                 {@link #clearAlarms(Context, int)}. The method will return if the listener
	 *                 does not like the context, aka
	 *                 {@link OnAlarmsClearFinishedListener#onAlarmsClearFinished()} returns false.
	 * @param alarmSetter the alarm setter used for scheduling.*/
	private static void setupAlarmsWithSetters(@NotNull Context context, int reminderID,
	                                           @NotNull OnAlarmsClearFinishedListener listener,
	                                           AlarmSetter alarmSetter) {
		clearAlarms(context, reminderID);
		if (!listener.onAlarmsClearFinished() || ElementsLibrary.doesNotHaveReminder(reminderID))
			return;
		Reminder reminder = ElementsLibrary.findReminderByID(reminderID);
		if (!reminder.isEnabled()) return;
		List<PendingIntent> alarmIntentsList = alarmIntents.get(reminderID);
		List<PendingIntent> notificationIntentsList = notificationIntents.get(reminderID);
		long intervalMillis = 86400000 * reminder.getRepeatPeriod();
		List<Long> triggerAtMillis = triggerAtMillis(reminder.alarmTimeMillis(), intervalMillis);
		if (reminder.isDelayed()) {
			// modify triggerAtMillis, setRepeating a delayed alarm
			long[] delayedInfo = delayedTriggerAtMillis(triggerAtMillis, intervalMillis);
			long delayedTriggerAtMillis = delayedInfo[0];
			int delayedIndex = (int) delayedInfo[1];
			PendingIntent alarmIntent = generateAlarmPendingIntent(context, reminderID,
					delayedIndex, true);
			alarmIntentsList.add(alarmIntent);
			alarmSetter.set(delayedTriggerAtMillis, alarmIntent, "delayed");
		}
		// setRepeating un-delayed alarms
		for (int i = 0; i < triggerAtMillis.size(); i++) {
			PendingIntent alarmIntent = generateAlarmPendingIntent(context, reminderID,
					i, false);
			alarmIntentsList.add(alarmIntent);
			alarmSetter.setRepeating(triggerAtMillis.get(i), intervalMillis,
					alarmIntent, "un-delayed");
		}
		if (Preferences.reminderAdvanceTime.isZero()) return;
		// setRepeating notifications
		for (long millis : triggerAtMillis) {
			millis -= Preferences.reminderAdvanceTime.millis();
			if (millis < System.currentTimeMillis()) millis += intervalMillis;
			PendingIntent notificationIntent = generateNotificationPendingIntent(context, reminderID);
			notificationIntentsList.add(notificationIntent);
			alarmSetter.setRepeating(millis, intervalMillis, notificationIntent,
					"notification");
		}
	}
	/** Set up the time-scheduled alarms and notifications about one specified reminder using
	 * {@link AlarmManager}.
	 * @param context the context from which the {@link AlarmManager} will be gotten and from which
	 *                the pending intents will start from.
	 * @param reminderID specifying the reminder whose alarms will be setRepeating up.*/
	private static void setupAlarmsInAlarmManager(@NotNull Context context, int reminderID) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		setupAlarmsWithSetters(context, reminderID, () -> alarmManager != null, new AlarmSetter() {
			@Override
			public void setRepeating(long triggerAtMillis, long intervalMillis,
			                         PendingIntent pendingIntent, String tag) {
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis,
						pendingIntent);
			}
			@Override
			public void set(long triggerAtMillis, PendingIntent pendingIntent, String tag) {
				alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
			}
		});
	}
	/** Set up the time-scheduled alarms and notifications about one specified reminder using
	 * {@link BackgroundThread}.
	 * @param context the context from which the pending intents will start from.
	 * @param reminderID specifying the reminder whose alarms will be setRepeating up.*/
	private static void setupAlarmsInBackgroundThread(Context context, int reminderID) {
		String head = "reminder" + reminderID;
		setupAlarmsWithSetters(context, reminderID, () -> true, new AlarmSetter() {
			@Override
			public void setRepeating(long triggerAtMillis, long intervalMillis,
			                         PendingIntent pendingIntent, String tag) {
				BackgroundThread.putTask(head + tag, () -> {
					if ((BackgroundThread.getStartTimeMillis() - triggerAtMillis) %
							    intervalMillis == 0)
						BackgroundThread.sendPendingIntent(context, pendingIntent);
				});
			}
			@Override
			public void set(long triggerAtMillis, PendingIntent pendingIntent, String tag) {
				BackgroundThread.putTask(head + tag, () -> {
					if (BackgroundThread.getStartTimeMillis() == triggerAtMillis)
						BackgroundThread.sendPendingIntent(context, pendingIntent);
				});
			}
		});
		BackgroundThread.start();
	}
	/** Setup alarms using {@link #setupAlarmsInAlarmManager(Context, int)} or
	 * {@link #setupAlarmsInBackgroundThread(Context, int)} according to
	 * {@link Preferences#systemService}.*/
	public static void setupAlarms(Context context, int reminderID) {
		if (Preferences.systemService) setupAlarmsInAlarmManager(context, reminderID);
		else setupAlarmsInBackgroundThread(context, reminderID);
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
			if (CalendarUtils.setToBeginning(millis, Calendar.DAY_OF_MONTH) ==
					    CalendarUtils.setToBeginning(currentMillis, Calendar.DAY_OF_MONTH))
				millis += intervalMillis;
			result.add(millis);
		}
		return result;
	}
	/** Add the min time by intervalMillis, and return the min time plus the delayed time.
	 * @param triggerAtMillis WILL be modified after invoking the method.
	 * @param intervalMillis will be added to the min value in triggerAtMillis.
	 * @return the min value in triggerAtMillis plus {@link Preferences#delayTime} in millis.*/
	@NotNull
	@Contract("_, _ -> new")
	private static long[] delayedTriggerAtMillis(@NotNull List<Long> triggerAtMillis,
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
		return new long[] {min + Preferences.delayTime.millis(), index};
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
		BackgroundThread.removeTask("reminder" + reminderID + "delayed");
		BackgroundThread.removeTask("reminder" + reminderID + "un-delayed");
		BackgroundThread.removeTask("reminder" + reminderID + "notification");
	}
	/** Invoke {@link #setupAlarms(Context, int)} for each reminder.*/
	public static void setupAllAlarms(@NotNull Context context) {
		if (Preferences.systemService)
			for (int ID = 0; !ElementsLibrary.reminderIDOutOfBound(ID); ID++)
				setupAlarmsInAlarmManager(context, ID);
		else
			for (int ID = 0; !ElementsLibrary.reminderIDOutOfBound(ID); ID++)
				setupAlarmsInBackgroundThread(context, ID);
	}
	/** @return a PendingIntent referring to {@link AlarmReceiver}.*/
	private static PendingIntent generateAlarmPendingIntent(Context context, int reminderID,
	                                                        int mealIDIndex, boolean clearDelay) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra("reminderID", reminderID);
		intent.putExtra("mealIDIndex", mealIDIndex);
		intent.putExtra("clearDelay", clearDelay);
		return PendingIntent.getBroadcast(context, 0x0520, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}
	/** @return a PendingIntent referring to {@link NotificationReceiver}.*/
	private static PendingIntent generateNotificationPendingIntent(Context context, int reminderID) {
		Intent intent = new Intent(context, NotificationReceiver.class);
		intent.putExtra("reminderID", reminderID);
		return PendingIntent.getBroadcast(context, 0x0520, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}
	/** An interface for those which can schedule tasks. A typical implementation is to use
	 * {@link AlarmManager}, but there is another implementation used in this app, which is to use
	 * {@link BackgroundThread}.*/
	private interface AlarmSetter {
		/** Schedule a repeating task. Information about the params can be seen in
		 * {@link AlarmManager#setRepeating(int, long, long, PendingIntent)}.
		 * @param tag used to help differ tasks when there are more than one task. It is
		 * unnecessarily used in the implementation if you have other means to deal with more than
		 * one task.*/
		void setRepeating(long triggerAtMillis, long intervalMillis, PendingIntent pendingIntent,
		                  String tag);
		/** Schedule a non-repeating task. Information about the params can be seen in
		 * {@link AlarmManager#set(int, long, PendingIntent)}.
		 * @param tag used to help differ tasks when there are more than one task. It is
		 * unnecessarily used in the implementation if you have other means to deal with more than
		 * one task.*/
		void set(long triggerAtMillis, PendingIntent pendingIntent, String tag);
	}
	private interface OnAlarmsClearFinishedListener {
		/** A callback invoked when {@link #clearAlarms(Context, int)} is finished.*/
		boolean onAlarmsClearFinished();
	}
}
