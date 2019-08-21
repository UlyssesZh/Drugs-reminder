package ulysses.apps.drugsreminder.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.jetbrains.annotations.NotNull;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.activities.MainActivity;
import ulysses.apps.drugsreminder.elements.IReminder;
import ulysses.apps.drugsreminder.elements.Reminder;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.preferences.Preferences;
/*import ulysses.apps.drugsreminder.receivers.DelayingReceiver;*/

public class NotificationService extends IntentService {
	public static final String CHANNEL_FOR_REMINDING_ADVANCE = "channelForRemindingAdvance";
	public static final String CHANNEL_FOR_AUTO_CLOSE_HINT = "channelForAutoCloseHint";
	public static final String CHANNEL_FOR_BACKGROUND_TASKS = "channelForBackgroundTasks";
	public static final int BACKGROUND_NOTIFICATION_ID = 0x1108;
	public NotificationService() {
		super("NotificationService");
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		createNotificationChannels(this);
		startForeground(BACKGROUND_NOTIFICATION_ID, backgroundTasksNotification(this));
		NotificationManagerCompat.from(this).cancel(BACKGROUND_NOTIFICATION_ID);
		int reminderID = intent.getIntExtra("reminderID", 0);
		IReminder reminder = ElementsLibrary.findReminderByID(reminderID);
		if (intent.getBooleanExtra("forRemindingAdvance", true))
			notifyForRemindingAdvance(intent, reminder);
		else notifyForAutoCloseHint(reminder);
	}
	private void notifyForRemindingAdvance(Intent intent, @NotNull IReminder reminder) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
				CHANNEL_FOR_REMINDING_ADVANCE);
		builder.setSmallIcon(R.drawable.ic_notification);
		builder.setContentTitle(getString(R.string.notification_for_reminding_advance_content_title_format,
				Preferences.reminderAdvanceTime.toString(getResources())));
		builder.setContentText(getString(R.string.notification_for_reminding_advance_content_text_format,
				reminder.drugsString(getResources())));
		builder.setPriority(NotificationCompat.PRIORITY_MAX);
		builder.setAutoCancel(true);
		setContentIntentFor(builder);
		/*// set the delay action for the notification
		Intent delayIntent = new Intent(this, DelayingReceiver.class);
		delayIntent.putExtras(intent);
		builder.addAction(R.drawable.ic_trending_down_white_24dp, getString(R.string.delay_format,
				Preferences.delayTime.toString(getResources())),
				PendingIntent.getBroadcast(this, 0x0520, delayIntent,
						PendingIntent.FLAG_UPDATE_CURRENT));*/
		// send a notification whose id is the reminder's ID
		NotificationManagerCompat.from(this).notify(reminder.getID(), builder.build());
	}
	private void notifyForAutoCloseHint(@NotNull IReminder reminder) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
				CHANNEL_FOR_AUTO_CLOSE_HINT);
		builder.setSmallIcon(R.drawable.ic_notification);
		builder.setContentTitle(getString(R.string.notification_for_auto_close_content_title));
		builder.setContentText(getString(R.string.notification_for_auto_close_content_text_format,
				reminder.drugsString(getResources())));
		builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
		builder.setAutoCancel(true);
		setContentIntentFor(builder);
		NotificationManagerCompat.from(this).notify(0x0520, builder.build());
	}
	public static Notification backgroundTasksNotification(Context context) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
				NotificationService.CHANNEL_FOR_BACKGROUND_TASKS);
		builder.setSmallIcon(R.drawable.ic_notification);
		builder.setAutoCancel(true);
		builder.setPriority(NotificationCompat.PRIORITY_LOW);
		builder.setContentTitle(context.getString(
				R.string.notification_for_background_tasks_content_title));
		/*builder.setContentText(context.getString(
				R.string.notification_for_background_tasks_content_text));
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		builder.setContentIntent(PendingIntent.getActivity(context, 0x0520, intent,
				PendingIntent.FLAG_UPDATE_CURRENT));*/
		return builder.build();
	}
	private void setContentIntentFor(@NotNull NotificationCompat.Builder builder) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		builder.setContentIntent(PendingIntent.getActivity(this, 0x0520,
				intent, PendingIntent.FLAG_UPDATE_CURRENT));
	}
	public static void createNotificationChannels(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channelForReminderAdvance =
					new NotificationChannel(CHANNEL_FOR_REMINDING_ADVANCE,
							context.getString(R.string.channel_for_reminding_advance_name),
							NotificationManager.IMPORTANCE_HIGH);
			NotificationChannel channelForAutoCloseHint =
					new NotificationChannel(CHANNEL_FOR_AUTO_CLOSE_HINT,
							context.getString(R.string.channel_for_auto_close_hint_name),
							NotificationManager.IMPORTANCE_DEFAULT);
			NotificationChannel channelForBackgroundTasks =
					new NotificationChannel(CHANNEL_FOR_BACKGROUND_TASKS,
							context.getString(R.string.channel_for_background_tasks_name),
							NotificationManager.IMPORTANCE_LOW);
			channelForReminderAdvance.setDescription(
					context.getString(R.string.channel_for_reminding_advance_description));
			channelForAutoCloseHint.setDescription(
					context.getString(R.string.channel_for_auto_close_hint_description));
			channelForBackgroundTasks.setDescription(
					context.getString(R.string.channel_for_background_tasks_description));
			NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
			notificationManager.createNotificationChannel(channelForReminderAdvance);
			notificationManager.createNotificationChannel(channelForAutoCloseHint);
			notificationManager.createNotificationChannel(channelForBackgroundTasks);
		}
	}
}
