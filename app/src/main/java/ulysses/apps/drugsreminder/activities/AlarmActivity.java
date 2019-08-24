package ulysses.apps.drugsreminder.activities;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.adapters.ImprovedSimpleAdapter;
import ulysses.apps.drugsreminder.elements.DelayedReminder;
import ulysses.apps.drugsreminder.elements.Drug;
import ulysses.apps.drugsreminder.elements.IReminder;
import ulysses.apps.drugsreminder.libraries.AlarmsLibrary;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.receivers.NotificationReceiver;
import ulysses.apps.drugsreminder.util.CalendarUtils;
import ulysses.apps.drugsreminder.util.Time;

public class AlarmActivity extends AppCompatActivity {
	private static int MESSAGE_WHAT = 0x0520;
	private MediaPlayer mediaPlayer;
	private Thread vibratingThread;
	private IReminder reminder;
	private int rootReminderID;
	private Timer timer;
	private long triggerAtMillis;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		wakeUp();
		Intent intent = getIntent();
		int reminderID = intent.getIntExtra("reminderID", 0);
		reminder = ElementsLibrary.findReminderByID(reminderID);
		if (!reminder.isRepeating()) {
			ElementsLibrary.deleteReminder(reminderID);
			rootReminderID = ((DelayedReminder) reminder).getReminderID();
		} else rootReminderID = reminder.getID();
		triggerAtMillis = intent.getLongExtra("triggerAtMillis",
				CalendarUtils.setToBeginning(System.currentTimeMillis(), Calendar.MINUTE));
		setupAudio();
		setupVibration();
		setupViews();
		Handler handler = new AlarmStopper(this, rootReminderID);
		timer = new Timer();
		if (!Preferences.autoCloseTime.isZero())
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					handler.sendEmptyMessage(MESSAGE_WHAT);
				}
			}, Preferences.autoCloseTime.millis());
	}
	@Override
	protected void onDestroy() {
		if (timer != null) timer.cancel();
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) mediaPlayer.stop();
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		if (vibratingThread != null && vibratingThread.isAlive()) vibratingThread.interrupt();
		super.onDestroy();
	}
	private void wakeUp() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
				                     WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
				                     WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
				                     WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}
	private void setupAudio() {
		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioManager == null) return;
		AudioAttributes.Builder attributesBuilder = new AudioAttributes.Builder();
		attributesBuilder.setUsage(AudioAttributes.USAGE_ALARM);
		attributesBuilder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
		mediaPlayer = MediaPlayer.create(this, Preferences.ringtoneUri, null,
				attributesBuilder.build(), audioManager.generateAudioSessionId());
		if (mediaPlayer == null) return;
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
		// setup toolbar
		Toolbar toolbar = findViewById(R.id.alarm_toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(view -> finish());
		// setup drugs list
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
		// setup delay buttons
		if (Preferences.delayMinutes == 0) {
			findViewById(R.id.alarm_delay).setVisibility(View.GONE);
		} else {
			setupDelayButton(findViewById(R.id.alarm_delay_button1), 1);
			setupDelayButton(findViewById(R.id.alarm_delay_button2), 2);
			setupDelayButton(findViewById(R.id.alarm_delay_button3), 3);
			setupDelayButton(findViewById(R.id.alarm_delay_button4), 4);
		}
	}
	private void setupDelayButton(@NotNull Button button, int multiplier) {
		int delayMinutes = multiplier * Preferences.delayMinutes;
		int daysNumber = delayMinutes / 720;
		button.setText((daysNumber == 0 ? "" : getString(R.string.day_format, daysNumber)) +
				               new Time(delayMinutes).toString(getResources()));
		long delayMillis = delayMinutes * 60000;
		button.setOnClickListener(view -> {
			ElementsLibrary.addReminder(new DelayedReminder(rootReminderID,
					triggerAtMillis + delayMillis));
			AlarmsLibrary.setupAllAlarms(this);
			finish();
		});
	}
	private static class AlarmStopper extends Handler {
		private AlarmActivity alarmActivity;
		private int reminderID;
		private AlarmStopper(AlarmActivity alarmActivity, int reminderID) {
			this.alarmActivity = alarmActivity;
			this.reminderID = reminderID;
		}
		@Override
		public void handleMessage(@NonNull Message message) {
			super.handleMessage(message);
			if (message.what == MESSAGE_WHAT) {
				Intent intent = new Intent(alarmActivity, NotificationReceiver.class);
				intent.putExtra("reminderID", reminderID);
				intent.putExtra("forRemindingAdvance", false);
				alarmActivity.sendBroadcast(intent);
				alarmActivity.finish();
			}
		}
	}
}
