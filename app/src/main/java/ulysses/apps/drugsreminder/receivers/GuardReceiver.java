package ulysses.apps.drugsreminder.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import ulysses.apps.drugsreminder.services.GuardService;
import ulysses.apps.drugsreminder.services.ProtectionService;
import ulysses.apps.drugsreminder.util.Constants;

public class GuardReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (!GuardService.isRunning()) {
			// start GuardService
			Intent guardIntent = new Intent(ProtectionService.ACTION_GUARD);
			String guardClassName = Constants.packageName + ".services.GuardService";
			guardIntent.setComponent(new ComponentName(Constants.packageName,
					guardClassName));
			context.startService(guardIntent);
			// start ProtectionService
			Intent protectionIntent = new Intent(GuardService.ACTION_PROTECTION);
			String protectionClassName = Constants.packageName + ".services.ProtectionService";
			protectionIntent.setComponent(new ComponentName(Constants.packageName,
					protectionClassName));
			context.startService(protectionIntent);
		}
	}
}
