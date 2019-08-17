/*package ulysses.apps.drugsreminder.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import ulysses.apps.drugsreminder.libraries.AlarmsLibrary;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;

public class DelayingService extends IntentService {
	public DelayingService() {
		super("DelayingService");
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		int reminderID = intent.getIntExtra("reminderID", 0);
		NotificationManagerCompat.from(this).cancel(reminderID);
		if (!ElementsLibrary.doesNotHaveReminder(reminderID)) {
			ElementsLibrary.findReminderByID(reminderID).delay();
			AlarmsLibrary.setupAlarms(this, reminderID);
		}
	}
}
*/