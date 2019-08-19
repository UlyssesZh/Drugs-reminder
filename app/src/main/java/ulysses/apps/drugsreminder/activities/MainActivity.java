package ulysses.apps.drugsreminder.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
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

public class MainActivity extends AppCompatActivity {
	private static final int OVERLAY_PERMISSION_REQUEST_CODE = 0x1314;
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
		// The listener is registered in onResume and unregistered in onPause
		preferencesListener = (sharedPreferences, key) -> Preferences.load(this);
		// load data from local files
		ElementsLibrary.loadElements(this);
		Preferences.load(this);
		// setup views
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this,
				getSupportFragmentManager());
		ViewPager viewPager = findViewById(R.id.main_view_pager);
		viewPager.setAdapter(sectionsPagerAdapter);
		((TabLayout) findViewById(R.id.tabs)).setupWithViewPager(viewPager);
		// start GuardService to protect BackgroundThread
		sendBroadcast(new Intent(this, GuardReceiver.class));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			// On Android Q (API 29) and above, the SYSTEM_ALERT_WINDOW permission is no longer
			// available, so say sorry!
			Toast.makeText(this, R.string.system_alert_window_permission_warning,
					Toast.LENGTH_LONG).show();
		} else if (!Settings.canDrawOverlays(this)) {
			// ask the user to grant the permission
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.overlay_permission_hint);
			builder.setPositiveButton(R.string.positive_text, (dialogInterface, i) -> {
				// show the interface of granting permissions
				try {
					Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
					startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			builder.setNegativeButton(R.string.negative_text, (dialogInterface, i) -> {});
			builder.create().show();
		}
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
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE)
			Toast.makeText(this, Settings.canDrawOverlays(this) ?
					                     R.string.overlay_permission_granted_hint :
					                     R.string.overlay_permission_ungranted_hint,
					Toast.LENGTH_LONG).show();
	}
}