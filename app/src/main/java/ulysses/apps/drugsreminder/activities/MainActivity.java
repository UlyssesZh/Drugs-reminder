package ulysses.apps.drugsreminder.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.libraries.AlarmsLibrary;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.adapters.SectionsPagerAdapter;
import ulysses.apps.drugsreminder.receivers.GuardReceiver;
import ulysses.apps.drugsreminder.services.NotificationService;
import ulysses.apps.drugsreminder.util.BackgroundThread;
import ulysses.apps.drugsreminder.util.PermissionPageUtils;

public class MainActivity extends AppCompatActivity {
	private SharedPreferences.OnSharedPreferenceChangeListener preferencesListener;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		// create notification channels
		NotificationService.createNotificationChannels(this);
		// initialize the background thread
		BackgroundThread.init();
		// make Preferences change its static fields when preferences is changed
		// The listener is registered onResume and unregistered onPause
		preferencesListener = (sharedPreferences, key) -> Preferences.load(this);
		// load data from local files
		ElementsLibrary.loadElements(this);
		Preferences.load(this);
		// ask for permissions
		if (!Preferences.saved) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.permission_hint);
			builder.setPositiveButton(R.string.positive_text,
					(dialogInterface, i) -> PermissionPageUtils.goPermissionPage(this));
			builder.setNegativeButton(R.string.negative_text, (dialogInterface, i) -> {});
			builder.create().show();
		}
		// save
		saveAll();
		// setup views
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this,
				getSupportFragmentManager());
		ViewPager viewPager = findViewById(R.id.main_view_pager);
		viewPager.setAdapter(sectionsPagerAdapter);
		((TabLayout) findViewById(R.id.tabs)).setupWithViewPager(viewPager);
		// start GuardService to protect BackgroundThread
		sendBroadcast(new Intent(this, GuardReceiver.class));
	}
	@Override
	protected void onResume() {
		super.onResume();
		saveAll();
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(preferencesListener);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		saveAll();
		PreferenceManager.getDefaultSharedPreferences(this)
				.unregisterOnSharedPreferenceChangeListener(preferencesListener);
	}
	private void saveAll() {
		ElementsLibrary.saveElements(this);
		Preferences.save(this);
		AlarmsLibrary.setupAllAlarms(this);
	}
}