package ulysses.apps.drugsreminder.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.libraries.AlarmsLibrary;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.adapters.SectionsPagerAdapter;
import ulysses.apps.drugsreminder.services.NotificationService;
import ulysses.apps.drugsreminder.util.BackgroundThread;

public class MainActivity extends AppCompatActivity {
	private SharedPreferences.OnSharedPreferenceChangeListener preferencesListener;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		NotificationService.createNotificationChannel(this);
		BackgroundThread.init();
		preferencesListener = (sharedPreferences, key) -> Preferences.load(this);
		ElementsLibrary.loadElements(this);
		Preferences.load(this);
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this,
				getSupportFragmentManager());
		ViewPager viewPager = findViewById(R.id.main_view_pager);
		viewPager.setAdapter(sectionsPagerAdapter);
		((TabLayout) findViewById(R.id.tabs)).setupWithViewPager(viewPager);
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