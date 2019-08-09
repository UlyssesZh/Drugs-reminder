package ulysses.apps.drugsreminder.services;

import android.app.IntentService;
import android.content.Intent;

import ulysses.apps.drugsreminder.libraries.AlarmsLibrary;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;

public class DelayingService extends IntentService {
	public DelayingService() {
		super("DelayingService");
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		int reminderID = intent.getIntExtra("reminderID", 0);
		if (!ElementsLibrary.doesNotHaveReminder(reminderID)) {
			ElementsLibrary.findReminderByID(reminderID).setDelayed(true);
			AlarmsLibrary.setupAlarms(this, reminderID);
		}
	}
}
