package ulysses.apps.drugsreminder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import ulysses.apps.drugsreminder.services.NotificationService;
import ulysses.apps.drugsreminder.util.LogUtils;

public class NotificationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, NotificationService.class);
		serviceIntent.setFlags(Intent.FLAG_FROM_BACKGROUND);
		serviceIntent.putExtras(intent);
		LogUtils.d("NotificationReceiver", "The notification for reminder " +
				                                   intent.getIntExtra("reminderID",
						                                   0) + " is received.");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			context.startForegroundService(serviceIntent);
		else context.startService(serviceIntent);
	}
}
