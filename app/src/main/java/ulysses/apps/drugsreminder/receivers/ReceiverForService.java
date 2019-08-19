package ulysses.apps.drugsreminder.receivers;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public abstract class ReceiverForService extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, targetServiceClass());
		serviceIntent.setAction(action());
		serviceIntent.setFlags(Intent.FLAG_FROM_BACKGROUND);
		serviceIntent.putExtras(intent);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			context.startForegroundService(serviceIntent);
		else
			context.startService(serviceIntent);
	}
	protected abstract Class<? extends Service> targetServiceClass();
	protected String action() {
		return null;
	}
}
