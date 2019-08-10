package ulysses.apps.drugsreminder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ulysses.apps.drugsreminder.activities.AlarmActivity;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent alarmIntent = new Intent(context, AlarmActivity.class);
		alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		alarmIntent.setAction(Intent.ACTION_MAIN);
		alarmIntent.putExtras(intent);
		context.startActivity(alarmIntent);
	}
}
