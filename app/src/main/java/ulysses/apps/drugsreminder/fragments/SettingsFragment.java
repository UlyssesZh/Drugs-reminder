package ulysses.apps.drugsreminder.fragments;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.activities.AboutActivity;
import ulysses.apps.drugsreminder.activities.QAndAActivity;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.util.AutoStartPageUtils;
import ulysses.apps.drugsreminder.util.LogUtils;
import ulysses.apps.drugsreminder.util.PermissionPageUtils;

public class SettingsFragment extends PreferenceFragmentCompat {
	private static final int RINGTONE_REQUEST_CODE = 0x0520;
	private Preference preferenceRingtone;
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.preferences);
		preferenceRingtone = findPreference("ringtoneUri");
		setOnClickListener(preferenceRingtone, preference -> {
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
		setOnClickListener("grantPermissions", preference -> {
			PermissionPageUtils.goPermissionPage(getActivity(), 0x1314);
			return true;
		});
		setOnClickListener("clearLog", preference -> {
			LogUtils.clear(getContext());
			return true;
		});
		setOnClickListener("openLog", preference -> {
			LogUtils.openLog(getContext());
			return true;
		});
		setOnClickListener("allowAutoStart", preference -> {
			AutoStartPageUtils.goAutoStartPage(getContext());
			return true;
		});
		setOnClickListener("qAndA", preference -> {
			Intent intent = new Intent(getContext(), QAndAActivity.class);
			startActivity(intent);
			return true;
		});
		setOnClickListener("about", preference -> {
			Intent intent = new Intent(getContext(), AboutActivity.class);
			startActivity(intent);
			return true;
		});
		setOnClickListener("resetSettings", preference -> {
			Context context = getContext();
			if (context == null) return false;
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(R.string.comfirm_reset_settings_hint);
			builder.setPositiveButton(R.string.positive_text, (dialogInterface, i) -> {
				Preferences.setDefault();
				Preferences.save(getContext());
				setChecked("vibration", Preferences.vibration);
				setChecked("resetStarting", Preferences.resetStarting);
			});
			builder.setNegativeButton(R.string.negative_text, null);
			builder.create().show();
			return true;
		});
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == RINGTONE_REQUEST_CODE && data != null) {
			Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			String uriString = uri == null ? "" : uri.toString();
			if (!preferenceRingtone.callChangeListener(uriString)) return;
			Preferences.ringtoneUri = uri;
			SharedPreferences.Editor editor = preferenceRingtone.getSharedPreferences().edit();
			editor.putString("ringtoneUri", uriString);
			editor.apply();
		}
	}
	private void setOnClickListener(Preference preference,
	                                Preference.OnPreferenceClickListener listener) {
		if (preference != null) preference.setOnPreferenceClickListener(listener);
	}
	private void setOnClickListener(CharSequence key,
	                                Preference.OnPreferenceClickListener listener) {
		setOnClickListener((Preference) findPreference(key), listener);
	}
	private void setChecked(CharSequence key, boolean checked) {
		SwitchPreferenceCompat preference = findPreference(key);
		if (preference != null) preference.setChecked(checked);
	}
}
