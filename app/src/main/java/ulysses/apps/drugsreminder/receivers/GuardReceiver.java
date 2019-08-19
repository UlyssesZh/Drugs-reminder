package ulysses.apps.drugsreminder.receivers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;

import ulysses.apps.drugsreminder.services.GuardService;

public class GuardReceiver extends ReceiverForService {
	public static String ACTION_GUARD = "ulysses.apps.drugsreminder.receivers.ACTION_GUARD";
	@Override
	public void onReceive(Context context, Intent intent) {
		if (!GuardService.isRunning()) super.onReceive(context, intent);
	}
	@Override
	protected Class<? extends Service> targetServiceClass() {
		return GuardService.class;
	}
	@Override
	protected String action() {
		return ACTION_GUARD;
	}
}
