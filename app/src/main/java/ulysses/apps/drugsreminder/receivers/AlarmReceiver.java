package ulysses.apps.drugsreminder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import org.jetbrains.annotations.NotNull;

import ulysses.apps.drugsreminder.activities.AlarmActivity;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.util.CalendarUtils;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, @NotNull Intent intent) {
		Intent alarmIntent = new Intent(context, AlarmActivity.class);
		int reminderID = intent.getIntExtra("reminderID", 0);
		NotificationManagerCompat.from(context).cancel(reminderID);
		alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
				                     Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT |
				                     Intent.FLAG_FROM_BACKGROUND);
		alarmIntent.setAction(Intent.ACTION_MAIN);
		alarmIntent.putExtras(intent);
		CalendarUtils.logMessage(
				intent.getLongExtra("triggerAtMillis", System.currentTimeMillis()),
				"reminder" + reminderID +
						(ElementsLibrary.findReminderByID(reminderID).isRepeating() ?
								 "un-delayed" : "delayed") + ", which triggers at %s, is received.");
		context.startActivity(alarmIntent);
	}
}
