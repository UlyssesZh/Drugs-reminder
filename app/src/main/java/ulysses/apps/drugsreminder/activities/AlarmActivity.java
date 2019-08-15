package ulysses.apps.drugsreminder.activities;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.adapters.ImprovedSimpleAdapter;
import ulysses.apps.drugsreminder.elements.Drug;
import ulysses.apps.drugsreminder.elements.Reminder;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.preferences.Preferences;

public class AlarmActivity extends AppCompatActivity {
	private static int MESSAGE_WHAT = 0x0520;
	private MediaPlayer mediaPlayer;
	private Thread vibratingThread;
	private Reminder reminder;
	private Timer timer;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		wakeUp();
		Intent intent = getIntent();
		reminder = ElementsLibrary.findReminderByID(intent.getIntExtra("reminderID", 0));
		if (intent.getBooleanExtra("clearDelay", false))
			reminder.undelay();
		setupAudio();
		setupVibration();
		setupViews();
		Handler handler = new AlarmStopper(this);
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(MESSAGE_WHAT);
			}
		}, 60000);
	}
	private void wakeUp() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
			setTurnScreenOn(true);
			setShowWhenLocked(true);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
					                     WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
					                     WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
					                     WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		}
	}
	private void setupAudio() {
		AudioAttributes.Builder attributesBuilder = new AudioAttributes.Builder();
		attributesBuilder.setUsage(AudioAttributes.USAGE_ALARM);
		attributesBuilder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioManager == null) return;
		mediaPlayer = MediaPlayer.create(this, Preferences.ringtoneUri, null,
				attributesBuilder.build(), audioManager.generateAudioSessionId());
		mediaPlayer.setLooping(true);
		mediaPlayer.start();
	}
	private void setupVibration() {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		if (vibrator == null) return;
		vibratingThread = new Thread(() -> {
			while (true) {
				vibrator.vibrate(625);
				try {
					synchronized (vibrator) {
						vibrator.wait(1250);
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		});
		if (Preferences.vibration) vibratingThread.start();
	}
	private void setupViews() {
		setContentView(R.layout.alarm_activity);
		Toolbar toolbar = findViewById(R.id.alarm_toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(view -> {
			shut();
			finish();
		});
		List<Integer> drugIDs = reminder.getDrugIDs();
		List<String> usageDosages = reminder.getUsageDosages();
		int listSize = drugIDs.size();
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>(listSize);
		for (int i = 0; i < listSize; i++) {
			Map<String, Object> listItem = new HashMap<String, Object>();
			Drug drug = ElementsLibrary.findDrugByID(drugIDs.get(i));
			listItem.put("name", drug.getName());
			listItem.put("bitmap", drug.getBitmap());
			listItem.put("usageDosages", usageDosages.get(i));
			listItems.add(listItem);
		}
		((ListView) findViewById(R.id.alarm_drugs_list)).setAdapter(new ImprovedSimpleAdapter(
				this, listItems, R.layout.alarm_drug_item,
				new String[] {"name", "bitmap", "usageDosages"},
				new int[] {R.id.alarm_drug_name, R.id.alarm_drug_image, R.id.alarm_drug_usage_dosage}));
	}
	@Override
	public void onBackPressed() {
		shut();
		super.onBackPressed();
	}
	private void shut() {
		if (timer != null) timer.cancel();
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		if (vibratingThread != null && vibratingThread.isAlive()) vibratingThread.interrupt();
	}
	private static class AlarmStopper extends Handler {
		private AlarmActivity alarmActivity;
		private AlarmStopper(AlarmActivity alarmActivity) {
			this.alarmActivity = alarmActivity;
		}
		@Override
		public void handleMessage(@NonNull Message message) {
			super.handleMessage(message);
			if (message.what == MESSAGE_WHAT)
				alarmActivity.shut();
		}
	}
}
