package ulysses.apps.drugsreminder.util;

import android.app.PendingIntent;
import android.content.Context;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/** It is a class that encapsulates a thread in which a series of tasks will run one by one, and it
 * waits for a while, and the series of tasks runs again, and the loop goes on. It is ensured that
 * the tasks run at an 'integral time point'. And the period is specified by {@link #periodField},
 * which accepts one of the {@link Calendar} fields like {@link Calendar#MINUTE}.*/
public class BackgroundThread {
	private static Thread thread;
	/** The tasks that are executed at the beginning of every period.*/
	private static Map<String, Runnable> tasks;
	/** One of the {@link Calendar} fields.*/
	private static int periodField;
	private static OnInterruptedListener listener;
	/** Records the starting time of every period. If the background thread is not currently
	 * running, it is -1.*/
	private static long startTimeMillis;
	private BackgroundThread() {}
	/** Be sure to call it before using the class. */
	public static void init() {
		thread = new Thread();
		tasks = new HashMap<String, Runnable>();
		periodField = Calendar.MINUTE;
		startTimeMillis = -1;
	}
	public static Runnable putTask(String name, Runnable runnable) {
		return tasks.put(name, runnable);
	}
	public static Runnable removeTask(String name) {
		return tasks.remove(name);
	}
	public static void setPeriodField(int periodField) {
		BackgroundThread.periodField = periodField;
	}
	@Contract(pure = true)
	public static int getPeriodField() {
		return periodField;
	}
	/** It is recommended to use it within the tasks because you can know when the task is executed
	 * in this way.*/
	@Contract(pure = true)
	public static long getStartTimeMillis() {
		return startTimeMillis;
	}
	/** Start the background thread. Do nothing if the thread is currently alive.
	 * @param lock an object used for synchronizing inside the thread.*/
	public static void start(Object lock) {
		if (isAlive()) return;
		thread = new Thread(() -> {
			synchronized (lock) {
				try {
					Calendar calendar = Calendar.getInstance();
					CalendarUtils.setToBeginning(calendar, periodField);
					while (true) {
						calendar.add(periodField, 1);
						startTimeMillis = calendar.getTimeInMillis();
						// wait for some time
						lock.wait(startTimeMillis - System.currentTimeMillis());
						// run the tasks one by one
						for (Runnable runnable : tasks.values()) runnable.run();
					}
				} catch (InterruptedException e) {
					startTimeMillis = -1;
					if (listener != null) listener.onInterrupted();
				}
			}
		}, "BackgroundThread");
		thread.start();
	}
	/** Start the background thread using a newly created object for synchronizing.*/
	public static void start() {
		start(new Object());
	}
	public static boolean isAlive() {
		return thread.isAlive();
	}
	public static void interrupt() {
		thread.interrupt();
	}
	public static void clearTasks() {
		tasks.clear();
	}
	public static void clearTasks(String regexp) {
		for (String key : tasks.keySet()) if (key.matches(regexp)) removeTask(key);
	}
	/** Equivalent to {@link PendingIntent#send()} except that it will catch the
	 * {@link android.app.PendingIntent.CanceledException}.*/
	public static void sendPendingIntent(Context context,  @NotNull PendingIntent pendingIntent) {
		try {
			pendingIntent.send(context, 0x0520, null);
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
	}
	public static void setOnInterruptedListener(OnInterruptedListener listener) {
		BackgroundThread.listener = listener;
	}
	public interface OnInterruptedListener {
		/** A callback invoked when the background thread is interrupted.*/
		void onInterrupted();
	}
}
