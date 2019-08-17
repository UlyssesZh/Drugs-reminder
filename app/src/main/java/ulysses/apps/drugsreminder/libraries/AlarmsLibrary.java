package ulysses.apps.drugsreminder.libraries;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import ulysses.apps.drugsreminder.elements.Reminder;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.receivers.AlarmReceiver;
import ulysses.apps.drugsreminder.receivers.NotificationReceiver;
import ulysses.apps.drugsreminder.util.BackgroundThread;

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
	private static void setupAlarmsWithSetter(@NotNull Context context, int reminderID,
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
		/*if (reminder.getDelayed() > 0) {
			// modify triggerAtMillis, set a delayed alarm
			int delayed = reminder.getDelayed();
			long[][] delayedInfo = delayedInfo(triggerAtMillis, intervalMillis, delayed);
			long[] delayedTriggerAtMillis = delayedInfo[0];
			long[] delayedIndex = delayedInfo[1];
			for (int i = 0; i < delayed; i++) {
				PendingIntent alarmIntent = generateAlarmPendingIntent(context, reminderID,
						(int) delayedIndex[i], true);
				alarmIntentsList.add(alarmIntent);
				alarmSetter.set(delayedTriggerAtMillis[i], alarmIntent, "delayed" + i);
			}
		}*/
		// set un-delayed alarms
		for (int i = 0; i < triggerAtMillis.size(); i++) {
			PendingIntent alarmIntent = generateAlarmPendingIntent(context, reminderID,
					i, false);
			alarmIntentsList.add(alarmIntent);
			alarmSetter.setRepeating(triggerAtMillis.get(i), intervalMillis,
					alarmIntent, "un-delayed" + i);
		}
		if (Preferences.reminderAdvanceTime.isZero()) return;
		// set notifications
		for (int i = 0; i < triggerAtMillis.size(); i++) {
			long millis = triggerAtMillis.get(i) - Preferences.reminderAdvanceTime.millis();
			if (millis < System.currentTimeMillis()) millis += intervalMillis;
			PendingIntent notificationIntent = generateNotificationPendingIntent(context, reminderID);
			notificationIntentsList.add(notificationIntent);
			alarmSetter.setRepeating(millis, intervalMillis, notificationIntent,
					"notification" + i);
		}
	}
	/** Set up the time-scheduled alarms and notifications about one specified reminder using
	 * {@link AlarmManager}.
	 * @param context the context from which the {@link AlarmManager} will be gotten and from which
	 *                the pending intents will start from.
	 * @param reminderID specifying the reminder whose alarms will be setRepeating up.*/
	private static void setupAlarmsInAlarmManager(@NotNull Context context, int reminderID) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		setupAlarmsWithSetter(context, reminderID, () -> alarmManager != null, new AlarmSetter() {
			@Override
			public void setRepeating(long triggerAtMillis, long intervalMillis,
			                         PendingIntent pendingIntent, String tag) {
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis,
						pendingIntent);
			}
			/*@Override
			public void set(long triggerAtMillis, PendingIntent pendingIntent, String tag) {
				alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
			}*/
		});
	}
	/** Set up the time-scheduled alarms and notifications about one specified reminder using
	 * {@link BackgroundThread}.
	 * @param context the context from which the pending intents will start from.
	 * @param reminderID specifying the reminder whose alarms will be setRepeating up.*/
	private static void setupAlarmsInBackgroundThread(Context context, int reminderID) {
		String head = "reminder" + reminderID;
		setupAlarmsWithSetter(context, reminderID, () -> true, new AlarmSetter() {
			@Override
			public void setRepeating(long triggerAtMillis, long intervalMillis,
			                         PendingIntent pendingIntent, String tag) {
				BackgroundThread.putTask(head + tag, () -> {
					long timeDifference = BackgroundThread.getStartTimeMillis() - triggerAtMillis;
					if (timeDifference >= 0 && timeDifference % intervalMillis == 0)
						sendPendingIntent(context, pendingIntent);
				});
			}
			/*@Override
			public void set(long triggerAtMillis, PendingIntent pendingIntent, String tag) {
				BackgroundThread.putTask(head + tag, () -> {
					if (BackgroundThread.getStartTimeMillis() == triggerAtMillis)
						sendPendingIntent(context, pendingIntent);
				});
			}*/
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
			result.add(millis);
		}
		return result;
	}
	/*/** Add the min time by intervalMillis, and return the min time plus the delayed time.
	 * @param triggerAtMillis WILL be modified after invoking the method.
	 * @param intervalMillis will be added to the min value in triggerAtMillis.
	 * @return an array whose [0] is the min values in triggerAtMillis plus
	 *         {@link Preferences#delayTime} in millis, and whose [1] is the indices of the min
	 *         values in triggerAtMillis.*/
	/*@Contract("_, _, _ -> new")
	@NotNull
	private static long[][] delayedInfo(@NotNull List<Long> triggerAtMillis, long intervalMillis,
	                                    int delayed) {
		long[] delayedTriggerAtMillis = new long[delayed];
		long[] delayedIndex = new long[delayed];
		for (int i = 0; i < delayed; i++) {
			long min = Long.MAX_VALUE;
			int index = 0;
			for (int j = 0; j < triggerAtMillis.size(); j++) {
				long millis = triggerAtMillis.get(j);
				if (millis < min) {
					min = millis;
					index = j;
				}
			}
			triggerAtMillis.set(index, min + intervalMillis);
			delayedTriggerAtMillis[i] = min + Preferences.delayTime.millis();
			delayedIndex[i] = index;
		}
		return new long[][] {delayedTriggerAtMillis, delayedIndex};
	}*/
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
		intent.putExtra("forRemindingAdvance", true);
		return PendingIntent.getBroadcast(context, 0x0520, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}
	/** Equivalent to {@link PendingIntent#send()} except that it will catch the
	 * {@link android.app.PendingIntent.CanceledException}.*/
	public static void sendPendingIntent(Context context,  @NotNull PendingIntent pendingIntent) {
		try {
			pendingIntent.send(context, 0x0520, null);
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
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
		/*/** Schedule a non-repeating task. Information about the params can be seen in
		 * {@link AlarmManager#set(int, long, PendingIntent)}.
		 * @param tag used to help differ tasks when there are more than one task. It is
		 * unnecessarily used in the implementation if you have other means to deal with more than
		 * one task.*/
		/*void set(long triggerAtMillis, PendingIntent pendingIntent, String tag);*/
	}
	private interface OnAlarmsClearFinishedListener {
		/** A callback invoked when {@link #clearAlarms(Context, int)} is finished.*/
		boolean onAlarmsClearFinished();
	}
}
