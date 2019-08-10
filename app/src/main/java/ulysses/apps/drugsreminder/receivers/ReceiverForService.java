package ulysses.apps.drugsreminder.receivers;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class ReceiverForService extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, targetServiceClass());
		serviceIntent.putExtras(intent);
		context.startService(serviceIntent);
	}
	protected abstract Class<? extends Service> targetServiceClass();
}
