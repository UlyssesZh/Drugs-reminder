package ulysses.apps.drugsreminder.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import ulysses.apps.drugsreminder.util.Constants;
import ulysses.apps.drugsreminder.util.IProcessConnection;

public class ProtectionService extends Service {
	public static final String ACTION_GUARD = Constants.packageName + ".ACTION_GUARD";
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			Log.d("ProtectionService", "connected!");
		}
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.d("ProtectionService", "disconnected!");
			startGuardService();
		}
	};
	@Override
	public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("ProtectionService", "created!");
		startForeground(NotificationService.BACKGROUND_NOTIFICATION_ID,
				NotificationService.backgroundTasksNotification(this));
		NotificationManagerCompat.from(this).cancel(NotificationService.BACKGROUND_NOTIFICATION_ID);
		startGuardService();
	}
	@Override
	public IBinder onBind(Intent intent) {
		return new IProcessConnection.Stub() {};
	}
	private void startGuardService() {
		new Thread(() -> {
			Intent guardIntent = new Intent(ACTION_GUARD);
			String className = Constants.packageName + ".services.GuardService";
			guardIntent.setComponent(new ComponentName(Constants.packageName, className));
			startService(guardIntent);
			bindService(guardIntent, serviceConnection, BIND_AUTO_CREATE);
		}).start();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("ProtectionService", "destroyed!");
		unbindService(serviceConnection);
	}
}
