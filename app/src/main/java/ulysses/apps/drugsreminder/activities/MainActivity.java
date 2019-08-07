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

public class MainActivity extends AppCompatActivity {
	public SharedPreferences elementsPreferences;
	public SharedPreferences settingsPreferences;
	private SharedPreferences.OnSharedPreferenceChangeListener settingsPreferencesListener;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		elementsPreferences = getSharedPreferences("settings", MODE_PRIVATE);
		PreferenceManager.setDefaultValues(this, "settings",
				MODE_PRIVATE, R.xml.preferences, true);
		settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		settingsPreferencesListener = (sharedPreferences, key) -> Preferences.load(this);
		ElementsLibrary.loadElements(elementsPreferences);
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
		settingsPreferences.registerOnSharedPreferenceChangeListener(settingsPreferencesListener);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		saveAll();
		settingsPreferences.unregisterOnSharedPreferenceChangeListener(settingsPreferencesListener);
	}
	private void saveAll() {
		ElementsLibrary.saveElements(elementsPreferences);
		Preferences.save(this);
		AlarmsLibrary.setupAllAlarms(this);
	}
}