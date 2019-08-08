package ulysses.apps.drugsreminder.libraries;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.SparseArray;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ulysses.apps.drugsreminder.BuildConfig;
import ulysses.apps.drugsreminder.elements.Drug;
import ulysses.apps.drugsreminder.elements.Reminder;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.receivers.AlarmReceiver;
import ulysses.apps.drugsreminder.util.BitmapCoder;

public final class AlarmsLibrary {
	private static SparseArray<List<PendingIntent>> alarmIntents = new SparseArray<List<PendingIntent>>();
	public static void setupAlarms(@NotNull Context context, int reminderID) {
		clearAlarmIntents(context, reminderID);
		if (!ElementsLibrary.doesNotHaveReminder(reminderID)) {
			Reminder reminder = ElementsLibrary.findReminderByID(reminderID);
			if (reminder.isEnabled()) {
				AlarmManager alarmManager =
						(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				List<PendingIntent> alarmIntentsList = alarmIntents.get(reminderID);
				long intervalMillis = 86400000 * reminder.getRepeatPeriod();
				List<Long> triggerAtMillis = triggerAtMillis(reminder.alarmTimeMillis(),
						reminder.isDelayed(), intervalMillis);
				if (BuildConfig.DEBUG)
					for (long millis : triggerAtMillis) {
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(millis);
						String text = String.format("Creating alarm at %04d-%02d-%02d %02d:%02d:%02d",
								calendar.get(Calendar.YEAR),
								calendar.get(Calendar.MONTH) + 1,
								calendar.get(Calendar.DAY_OF_MONTH),
								calendar.get(Calendar.HOUR_OF_DAY),
								calendar.get(Calendar.MINUTE),
								calendar.get(Calendar.SECOND));
						Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
					}
				for (long millis : triggerAtMillis) {
					PendingIntent alarmIntent = generateAlarmIntent(context, reminder);
					alarmIntentsList.add(alarmIntent);
					alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, millis,
							intervalMillis, alarmIntent);
				}
			}
		}
	}
	private static List<Long> triggerAtMillis(@NotNull List<Long> alarmTimeMillis, boolean delayed,
	                                          long intervalMillis) {
		List<Long> result = new ArrayList<Long>(alarmTimeMillis.size());
		long currentMillis = System.currentTimeMillis();
		for (long millis : alarmTimeMillis) {
			if (currentMillis > millis + 60000)
				millis += ((currentMillis - millis) / intervalMillis + 1) * intervalMillis;
			result.add(millis);
		}
		if (delayed) {
			long min = Long.MAX_VALUE;
			int index = 0;
			for (int i = 0; i < result.size(); i++) {
				long millis = result.get(i);
				if (millis < min) {
					min = millis;
					index = i;
				}
			}
			result.set(index, min + Preferences.delayTime.millis());
		}
		return result;
	}
	private static void clearAlarmIntents(Context context, int reminderID) {
		List<PendingIntent> alarmIntentsList = alarmIntents.get(reminderID);
		if (alarmIntentsList != null) {
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			for (PendingIntent alarmIntent : alarmIntentsList) alarmManager.cancel(alarmIntent);
		}
		alarmIntents.setValueAt(reminderID, null);
		if (!ElementsLibrary.doesNotHaveReminder(reminderID)) {
			Reminder reminder = ElementsLibrary.findReminderByID(reminderID);
			if (reminder.isEnabled()) {
				alarmIntents.put(reminderID,
						new ArrayList<PendingIntent>(reminder.getMealIDs().size()));
			}
		}
	}
	public static void setupAllAlarms(@NotNull Context context) {
		for (int ID = 0; !ElementsLibrary.reminderIDOutOfBound(ID); ID++)
			setupAlarms(context, ID);
	}
	private static PendingIntent generateAlarmIntent(Context context, @NotNull Reminder reminder) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		List<Integer> drugIDs = reminder.getDrugIDs();
		int drugsNumber = drugIDs.size();
		intent.putExtra("drugsNumber", drugsNumber);
		List<String> drugNames = new ArrayList<String>(drugsNumber);
		List<String> drugBitmaps = new ArrayList<String>(drugsNumber);
		for (int i = 0; i < drugsNumber; i++) {
			Drug drug = ElementsLibrary.findDrugByID(drugIDs.get(i));
			drugNames.add(drug.getName());
			drugBitmaps.add(BitmapCoder.code(drug.getBitmap()));
		}
		String[] anArray = new String[0];
		intent.putExtra("drugNames", drugNames.toArray(anArray));
		intent.putExtra("drugBitmaps", drugBitmaps.toArray(anArray));
		intent.putExtra("usageDosages", reminder.getUsageDosages().toArray(anArray));
		intent.putExtra("vibration", Preferences.vibration);
		Uri ringtoneUri = Preferences.ringtoneUri;
		intent.putExtra("ringtoneUri", ringtoneUri == null ? "" : ringtoneUri.toString());
		return PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
