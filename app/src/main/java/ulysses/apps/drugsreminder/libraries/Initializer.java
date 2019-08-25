package ulysses.apps.drugsreminder.libraries;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;

import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.services.NotificationService;
import ulysses.apps.drugsreminder.util.BackgroundThread;
import ulysses.apps.drugsreminder.util.LogUtils;

public final class Initializer {
	public static void init(Context context, OnInitializedListener listener) {
		AsyncTask.execute(() -> {
			Looper.prepare();
			ElementsLibrary.init(context);
			Preferences.init(context);
			NotificationService.init(context);
			BackgroundThread.init();
			LogUtils.init(context);
			if (listener != null) listener.onInitialized();
		});
	}
	public static void init(Context context) {
		init(context, null);
	}
	public interface OnInitializedListener {
		void onInitialized();
	}
}
