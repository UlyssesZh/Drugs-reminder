package ulysses.apps.drugsreminder.activities;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.adapters.ImprovedSimpleAdapter;
import ulysses.apps.drugsreminder.util.BitmapCoder;

public class AlarmActivity extends AppCompatActivity {
	MediaPlayer mediaPlayer;
	Thread vibratingThread;
	Intent intent;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_activity);
		intent = getIntent();
		setupAudio();
		setupVibration();
		setupViews();
	}
	private void setupAudio() {
		AudioAttributes.Builder attributesBuilder = new AudioAttributes.Builder();
		attributesBuilder.setUsage(AudioAttributes.USAGE_ALARM);
		attributesBuilder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
		mediaPlayer = MediaPlayer.create(this,
				Uri.parse(intent.getStringExtra("ringtoneUri")), null,
				attributesBuilder.build(),
				((AudioManager) getSystemService(AUDIO_SERVICE)).generateAudioSessionId());
		mediaPlayer.setLooping(true);
		mediaPlayer.start();
	}
	private void setupVibration() {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibratingThread = new Thread(() -> {
			Object lock = new Object();
			while (true) {
				vibrator.vibrate(625);
				try {
					synchronized (lock) {
						lock.wait(1250);
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		});
		if (intent.getBooleanExtra("vibration", true)) vibratingThread.start();
	}
	private void setupViews() {
		Toolbar toolbar = findViewById(R.id.alarm_toolbar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(view -> {
			mediaPlayer.stop();
			if (vibratingThread.isAlive()) vibratingThread.interrupt();
			finish();
		});
		int listSize = intent.getIntExtra("drugsNumber", 0);
		String[] drugNames = intent.getStringArrayExtra("drugNames");
		String[] drugBitmaps = intent.getStringArrayExtra("drugBitmaps");
		String[] usageDosages = intent.getStringArrayExtra("usageDosages");
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>(listSize);
		for (int i = 0; i < listSize; i++) {
			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put("name", drugNames[i]);
			listItem.put("bitmap", BitmapCoder.decode(drugBitmaps[i]));
			listItem.put("usageDosages", usageDosages[i]);
			listItems.add(listItem);
		}
		((ListView) findViewById(R.id.alarm_drugs_list)).setAdapter(new ImprovedSimpleAdapter(
				this, listItems, R.layout.alarm_drug_item,
				new String[] {"name", "bitmap", "usageDosages"},
				new int[] {R.id.alarm_drug_name, R.id.alarm_drug_image, R.id.alarm_drug_usage_dosage}));
	}
	@Override
	public void onBackPressed() {
		if (mediaPlayer != null) mediaPlayer.stop();
		if (vibratingThread != null && vibratingThread.isAlive()) vibratingThread.interrupt();
		super.onBackPressed();
	}
}
