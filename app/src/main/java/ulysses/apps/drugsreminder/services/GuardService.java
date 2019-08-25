package ulysses.apps.drugsreminder.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import org.jetbrains.annotations.Contract;

import ulysses.apps.drugsreminder.BuildConfig;
import ulysses.apps.drugsreminder.libraries.AlarmsLibrary;
import ulysses.apps.drugsreminder.util.BackgroundThread;
import ulysses.apps.drugsreminder.util.IProcessConnection;
import ulysses.apps.drugsreminder.util.LogUtils;

public class GuardService extends Service {
	public static final String ACTION_PROTECTION = BuildConfig.APPLICATION_ID + ".ACTION_PROTECTION";
	private static PowerManager.WakeLock wakeLock;
	private static boolean running;
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			LogUtils.d("GuardService", "connected!");
		}
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			LogUtils.d("GuardService", "disconnected!");
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
					acquireWakeLock();
					try {
						lock.wait(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (!BackgroundThread.isAlive()) {
						AlarmsLibrary.setupAllAlarms(this);
						LogUtils.d("GuardService", "BackgroundThread is not alive.");
					} else LogUtils.d("GuardService", "BackgroundThread is alive.");
					releaseWakeLock();
				}
				checkBackgroundThread();
			}
		}).start();
	}
	private void startProtectionService() {
		new Thread(() -> {
			Intent protectionIntent = new Intent(ACTION_PROTECTION);
			String className = BuildConfig.APPLICATION_ID + ".services.ProtectionService";
			protectionIntent.setComponent(new ComponentName(BuildConfig.APPLICATION_ID,
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
		try {
			unbindService(serviceConnection);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		super.onDestroy();
		running = false;
	}
	@Contract(pure = true)
	public static boolean isRunning() {
		return running;
	}
	private void acquireWakeLock() {
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		if (powerManager != null) {
			wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					"drugsreminder:guardWakeLock");
			wakeLock.acquire(60000);
		}
	}
	private void releaseWakeLock() {
		if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
	}
}
