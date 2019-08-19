package ulysses.apps.drugsreminder.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.jetbrains.annotations.Contract;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.activities.MainActivity;
import ulysses.apps.drugsreminder.libraries.AlarmsLibrary;
import ulysses.apps.drugsreminder.receivers.GuardReceiver;
import ulysses.apps.drugsreminder.util.BackgroundThread;

public class GuardService extends IntentService {
	private static boolean running;
	public GuardService() {
		super("GuardService");
	}
	@Override
	protected synchronized void onHandleIntent(Intent intent) {
		running = true;
		// startForeground to avoid a death
		startForeground(0x1108, NotificationService.backgroundTasksNotification(this));
		Log.d("GuardService", "GuardService has started.");
		// continuously checking if BackgroundThread is alive
		while (true) {
			try {
				wait(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Log.d("GuardService", "GuardService is running.");
			if (!BackgroundThread.isAlive()) AlarmsLibrary.setupAllAlarms(this);
		}
	}
	@Override
	public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		running = false;
		// in case it is killed, ask others to revive it
		sendBroadcast(new Intent(this, GuardReceiver.class));
	}
	@Contract(pure = true)
	public static boolean isRunning() {
		return running;
	}
}
