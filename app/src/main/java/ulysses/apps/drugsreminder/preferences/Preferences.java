package ulysses.apps.drugsreminder.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.preference.PreferenceManager;

import ulysses.apps.drugsreminder.util.Time;

public final class Preferences {
	public static final String STARTING_TIME_TYPE_NEXT = "next";
	public static final String STARTING_TIME_TYPE_THAT = "that";
	public static final String STARTING_TIME_TYPE_PICK = "pick";
	private static SharedPreferences.OnSharedPreferenceChangeListener preferencesListener;
	public static Time reminderAdvanceTime;
	public static Uri ringtoneUri;
	public static boolean vibration;
	public static String startingTimeType;
	public static int delayMinutes;
	public static boolean resetStarting;
	public static int defaultFrequency;
	public static Time autoCloseTime;
	public static boolean saved = false;
	public static void init(Context context) {
		initListener(context);
		load(context);
	}
	private static void initListener(Context context) {
		preferencesListener = (sharedPreferences, key) -> load(context);
	}
	public static void registerListener(Context context) {
		if (preferencesListener == null) initListener(context);
		PreferenceManager.getDefaultSharedPreferences(context)
				.registerOnSharedPreferenceChangeListener(preferencesListener);
	}
	public static void unregisterListener(Context context) {
		if (preferencesListener == null) initListener(context);
		PreferenceManager.getDefaultSharedPreferences(context)
				.unregisterOnSharedPreferenceChangeListener(preferencesListener);
	}
	public static void setDefault() {
		reminderAdvanceTime = new Time(0, 30);
		ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		vibration = true;
		startingTimeType = STARTING_TIME_TYPE_NEXT;
		delayMinutes = 20;
		resetStarting = true;
		defaultFrequency = 1;
		autoCloseTime = new Time(0, 5);
	}
	public static void save(Context context) {
		SharedPreferences.Editor editor =
				PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("saved", true);
		editor.putInt("advanceTimeMinutes", reminderAdvanceTime.minutes());
		editor.putString("ringtoneUri", ringtoneUri == null ? "" : ringtoneUri.toString());
		editor.putBoolean("vibration", vibration);
		editor.putString("startingTimeType", startingTimeType);
		editor.putInt("delayMinutes", delayMinutes);
		editor.putBoolean("resetStarting", resetStarting);
		editor.putString("defaultFrequency", String.valueOf(defaultFrequency));
		editor.putInt("autoCloseTimeMinutes", autoCloseTime.minutes());
		editor.apply();
	}
	private static void load(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (saved = preferences.getBoolean("saved", false)) {
			reminderAdvanceTime = new Time(preferences.getInt("advanceTimeMinutes", 30));
			String uriString = preferences.getString("ringtoneUri", "");
			ringtoneUri = uriString.isEmpty() ?
					              RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) :
					              Uri.parse(uriString);
			vibration = preferences.getBoolean("vibration", true);
			startingTimeType = preferences.getString("startingTimeType",
					STARTING_TIME_TYPE_NEXT);
			delayMinutes = preferences.getInt("delayMinutes", 30);
			resetStarting = preferences.getBoolean("resetStarting", true);
			String defaultFrequencyString = preferences.getString("defaultFrequency", "1");
			try {
				defaultFrequency = Integer.valueOf(defaultFrequencyString);
			} catch (NumberFormatException e) {
				defaultFrequency = 1;
			}
			if (defaultFrequency <= 0) defaultFrequency = 1;
			autoCloseTime = new Time(preferences.getInt("autoCloseTimeMinutes", 5));
		} else setDefault();
	}
}
