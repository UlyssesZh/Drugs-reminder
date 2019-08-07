package ulysses.apps.drugsreminder.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.activities.AboutActivity;
import ulysses.apps.drugsreminder.preferences.Preferences;

public class SettingsFragment extends PreferenceFragmentCompat {
	private static final int RINGTONE_REQUEST_CODE = 0x0520;
	private Preference preferenceRingtone;
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.preferences);
		preferenceRingtone = findPreference("ringtoneUri");
		preferenceRingtone.setOnPreferenceClickListener(preference -> {
			Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Preferences.ringtoneUri);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
					RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, preference.getTitle());
			startActivityForResult(intent, RINGTONE_REQUEST_CODE);
			return true;
		});
		findPreference("about").setOnPreferenceClickListener(preference -> {
			Intent intent = new Intent(getContext(), AboutActivity.class);
			startActivity(intent);
			return true;
		});
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == RINGTONE_REQUEST_CODE) {
			if (data != null) {
				Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				String uriString = uri == null ? "" : uri.toString();
				if (preferenceRingtone.callChangeListener(uriString)) {
					Preferences.ringtoneUri = uri;
					SharedPreferences.Editor editor = preferenceRingtone.getSharedPreferences().edit();
					editor.putString("ringtoneUri", uriString);
					editor.apply();
				}
			}
		}
	}
}
