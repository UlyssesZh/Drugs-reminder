package ulysses.apps.drugsreminder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import ulysses.apps.drugsreminder.services.NotificationService;

public class NotificationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, NotificationService.class);
		serviceIntent.setFlags(Intent.FLAG_FROM_BACKGROUND);
		serviceIntent.putExtras(intent);
		Log.d("NotificationReceiver", "A notification is received.");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			context.startForegroundService(serviceIntent);
		else context.startService(serviceIntent);
	}
}
