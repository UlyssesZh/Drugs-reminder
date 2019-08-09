package ulysses.apps.drugsreminder.receivers;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class ReceiverForService extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent intent1 = new Intent(context, targetServiceClass());
		intent1.putExtras(intent);
		context.startService(intent1);
	}
	protected abstract Class<? extends Service> targetServiceClass();
}
