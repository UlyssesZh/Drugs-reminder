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
import ulysses.apps.drugsreminder.receivers.DelayingReceiver;

public class NotificationService extends IntentService {
	private static final String CHANNEL_ID = "channelForDrugsReminder";
	public NotificationService() {
		super("NotificationService");
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		createNotificationChannel(this);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
		builder.setSmallIcon(R.drawable.ic_launcher_foreground);
		builder.setContentTitle(getString(R.string.notification_content_title));
		builder.setContentText(getString(R.string.notification_content_text));
		builder.setPriority(NotificationCompat.PRIORITY_MAX);
		Intent delayIntent = new Intent(this, DelayingReceiver.class);
		delayIntent.putExtras(intent);
		builder.addAction(R.drawable.ic_launcher_foreground, getString(R.string.delay),
				PendingIntent.getBroadcast(this, 0x0520, delayIntent,
						PendingIntent.FLAG_UPDATE_CURRENT));
		// send a notification whose id is the reminder's ID
		NotificationManagerCompat.from(this).notify(
				intent.getIntExtra("reminderID", 0), builder.build());
	}
	public static void createNotificationChannel(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
					context.getString(R.string.notification_channel_name),
					NotificationManager.IMPORTANCE_DEFAULT);
			channel.setDescription(context.getString(R.string.notification_channel_description));
			NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
			if (notificationManager != null) notificationManager.createNotificationChannel(channel);
		}
	}
}
