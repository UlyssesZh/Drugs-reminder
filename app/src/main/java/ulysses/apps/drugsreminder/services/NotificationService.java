package ulysses.apps.drugsreminder.services;

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ulysses.apps.drugsreminder.BuildConfig;
import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.receivers.DelayingReceiver;

public class NotificationService extends IntentService {
	private static final String CHANNEL_ID = "channelForDrugsReminder";
	public NotificationService() {
		super("NotificationService");
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		createNotificationChannel();
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
		builder.setSmallIcon(R.drawable.ic_launcher_foreground);
		builder.setContentTitle(getString(R.string.notification_content_title));
		builder.setContentText(getString(R.string.notification_content_text));
		Intent intent1 = new Intent(this, DelayingReceiver.class);
		intent1.putExtras(intent);
		builder.addAction(R.drawable.ic_arrow_back_black_24dp, getString(R.string.delay),
				PendingIntent.getBroadcast(this, 0x0520, intent1,
						PendingIntent.FLAG_UPDATE_CURRENT));
		NotificationManagerCompat.from(this).notify(0x0520, builder.build());
		if (BuildConfig.DEBUG) {
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setMessage("Hoo! The notification has sent!");
			builder1.create().show();
		}
	}
	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
					getString(R.string.notification_channel_name),
					NotificationManager.IMPORTANCE_DEFAULT);
			channel.setDescription(getString(R.string.notification_channel_description));
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			if (notificationManager != null) notificationManager.createNotificationChannel(channel);
		}
	}
}
