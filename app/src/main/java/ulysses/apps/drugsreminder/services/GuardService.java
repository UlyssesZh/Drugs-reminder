package ulysses.apps.drugsreminder.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import org.jetbrains.annotations.Contract;

import ulysses.apps.drugsreminder.libraries.AlarmsLibrary;
import ulysses.apps.drugsreminder.util.BackgroundThread;
import ulysses.apps.drugsreminder.util.Constants;
import ulysses.apps.drugsreminder.util.IProcessConnection;

public class GuardService extends Service {
	public static final String ACTION_PROTECTION = Constants.packageName + ".ACTION_PROTECTION";
	private static boolean running;
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			Log.d("GuardService", "connected!");
		}
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.d("GuardService", "disconnected!");
			startProtectionService();
		}
	};
	@Override
	public void onCreate() {
		running = true;
		startForeground(NotificationService.BACKGROUND_NOTIFICATION_ID,
				NotificationService.backgroundTasksNotification(this));
		NotificationManagerCompat.from(this).cancel(NotificationService.BACKGROUND_NOTIFICATION_ID);
		checkBackgroundThread();
		startProtectionService();
	}
	@Override
	public IBinder onBind(Intent intent) {
		return new IProcessConnection.Stub() {};
	}
	private void checkBackgroundThread() {
		Object lock = new Object();
		new Thread(() -> {
			synchronized (lock) {
				while (!Thread.interrupted()) {
					try {
						lock.wait(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (!BackgroundThread.isAlive()) AlarmsLibrary.setupAllAlarms(this);
				}
				checkBackgroundThread();
			}
		}).start();
	}
	private void startProtectionService() {
		new Thread(() -> {
			Intent protectionIntent = new Intent(ACTION_PROTECTION);
			String className = Constants.packageName + ".services.ProtectionService";
			protectionIntent.setComponent(new ComponentName(Constants.packageName,
					className));
			startService(protectionIntent);
			bindService(protectionIntent, serviceConnection, BIND_AUTO_CREATE);
		}).start();
	}
	@Override
	public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}
	@Override
	public void onDestroy() {
		unbindService(serviceConnection);
		super.onDestroy();
		running = false;
	}
	@Contract(pure = true)
	public static boolean isRunning() {
		return running;
	}
}
