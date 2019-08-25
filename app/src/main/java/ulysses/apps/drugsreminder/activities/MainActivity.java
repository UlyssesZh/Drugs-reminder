package ulysses.apps.drugsreminder.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.libraries.AlarmsLibrary;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.libraries.Initializer;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.adapters.SectionsPagerAdapter;
import ulysses.apps.drugsreminder.receivers.GuardReceiver;
import ulysses.apps.drugsreminder.util.PermissionPageUtils;

public class MainActivity extends AppCompatActivity {
	private boolean initialized = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		// init all
		Initializer.init(this, () -> {
			initialized = true;
			if (!Preferences.saved) askForPermission();
		});
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
		Preferences.registerListener(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		saveAll();
		Preferences.unregisterListener(this);
	}
	private void saveAll() {
		if (initialized) {
			ElementsLibrary.saveElements(this);
			Preferences.save(this);
			AlarmsLibrary.setupAllAlarms(this);
		}
	}
	private void askForPermission() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.permission_hint);
		builder.setPositiveButton(R.string.positive_text,
				(dialogInterface, i) -> PermissionPageUtils.goPermissionPage(this,
						0x1314));
		builder.setNegativeButton(R.string.negative_text, null);
		builder.create().show();
	}
}
