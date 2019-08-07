package ulysses.apps.drugsreminder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ulysses.apps.drugsreminder.activities.AlarmActivity;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent intent1 = new Intent(context, AlarmActivity.class);
		intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent1.setAction(Intent.ACTION_MAIN);
		intent1.putExtras(intent);
		context.startActivity(intent1);
	}
}
