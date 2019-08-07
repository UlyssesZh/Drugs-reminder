package ulysses.apps.drugsreminder.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import ulysses.apps.drugsreminder.util.Time;

public final class Preferences {
	public static final String STARTING_TIME_TYPE_NEXT = "next";
	public static final String STARTING_TIME_TYPE_THAT = "that";
	public static final String STARTING_TIME_TYPE_PICK = "pick";
	public static Time reminderAdvanceTime;
	public static Uri ringtoneUri;
	public static boolean vibration;
	public static String startingTimeType;
	public static Time delayTime;
	public static boolean clearDelay;
	public static boolean resetStarting;
	public static void setDefault() {
		reminderAdvanceTime = new Time(0, 30);
		ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		vibration = true;
		startingTimeType = STARTING_TIME_TYPE_NEXT;
		delayTime = new Time(0, 30);
		clearDelay = true;
		resetStarting = true;
	}
	public static void save(Context context) {
		SharedPreferences.Editor editor =
				PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("saved", true);
		editor.putInt("advanceTimeMinutes", reminderAdvanceTime.minutes());
		editor.putString("ringtoneUri", ringtoneUri == null ? "" : ringtoneUri.toString());
		editor.putBoolean("vibration", vibration);
		editor.putString("startingTimeType", startingTimeType);
		editor.putInt("delayMinutes", delayTime.minutes());
		editor.putBoolean("clearDelay", clearDelay);
		editor.putBoolean("resetStarting", resetStarting);
		editor.apply();
	}
	public static void load(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (preferences.getBoolean("saved", false)) {
			reminderAdvanceTime = new Time(preferences.getInt("advanceTimeMinutes", 30));
			String uriString = preferences.getString("ringtoneUri", "");
			ringtoneUri = uriString.isEmpty() ?
					              RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) :
					              Uri.parse(uriString);
			vibration = preferences.getBoolean("vibration", true);
			startingTimeType = preferences.getString("startingTimeType",
					STARTING_TIME_TYPE_NEXT);
			delayTime = new Time(preferences.getInt("delayMinutes", 30));
			clearDelay = preferences.getBoolean("clearDelay", true);
			resetStarting = preferences.getBoolean("resetStarting", true);
		} else setDefault();
	}
}
