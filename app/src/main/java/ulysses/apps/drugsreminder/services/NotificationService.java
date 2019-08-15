package ulysses.apps.drugsreminder.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.activities.MainActivity;
import ulysses.apps.drugsreminder.elements.Reminder;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.receivers.DelayingReceiver;

public class NotificationService extends IntentService {
	private static final String CHANNEL_ID = "channelForDrugsReminder";
	public NotificationService() {
		super("NotificationService");
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		createNotificationChannel(this);
		int reminderID = intent.getIntExtra("reminderID", 0);
		Reminder reminder = ElementsLibrary.findReminderByID(reminderID);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
		builder.setSmallIcon(R.drawable.ic_notification);
		builder.setContentTitle(getString(R.string.notification_content_title_format,
				Preferences.delayTime.toString(getResources())));
		builder.setContentText(getString(R.string.notification_content_text_format,
				reminder.drugsString(getResources())));
		builder.setPriority(NotificationCompat.PRIORITY_MAX);
		builder.setAutoCancel(true);
		// set the tap action for the notification
		Intent tapIntent = new Intent(this, MainActivity.class);
		tapIntent.setAction(Intent.ACTION_MAIN);
		tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		builder.setContentIntent(PendingIntent.getActivity(this, 0x0520,
				tapIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		// set the delay action for the notification
		Intent delayIntent = new Intent(this, DelayingReceiver.class);
		delayIntent.putExtras(intent);
		builder.addAction(R.drawable.ic_trending_down_white_24dp, getString(R.string.delay_format,
				Preferences.delayTime.toString(getResources())),
				PendingIntent.getBroadcast(this, 0x0520, delayIntent,
						PendingIntent.FLAG_UPDATE_CURRENT));
		// send a notification whose id is the reminder's ID
		NotificationManagerCompat.from(this).notify(reminderID, builder.build());
	}
	public static void createNotificationChannel(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
					context.getString(R.string.notification_channel_name),
					NotificationManager.IMPORTANCE_HIGH);
			channel.setDescription(context.getString(R.string.notification_channel_description));
			NotificationManager notificationManager =
					context.getSystemService(NotificationManager.class);
			if (notificationManager != null) notificationManager.createNotificationChannel(channel);
		}
	}
}
