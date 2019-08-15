package ulysses.apps.drugsreminder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import ulysses.apps.drugsreminder.activities.AlarmActivity;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent alarmIntent = new Intent(context, AlarmActivity.class);
		Log.d("AlarmReceiver", "onReceive: The alarm has been received.");
		NotificationManagerCompat.from(context).cancel(intent.getIntExtra("reminderID", 0));
		alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
				                     Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT |
				                     Intent.FLAG_FROM_BACKGROUND);
		alarmIntent.setAction(Intent.ACTION_MAIN);
		alarmIntent.putExtras(intent);
		context.startActivity(alarmIntent);
	}
}
