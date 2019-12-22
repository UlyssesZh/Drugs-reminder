package ulysses.apps.drugsreminder.util;

import android.util.ArraySet;

import org.jetbrains.annotations.Contract;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** It is a class that encapsulates a thread in which a series of tasks will run one by one, and it
 * waits for a while, and the series of tasks runs again, and the loop goes on. It is ensured that
 * the tasks run at an 'integral time point'. And the period is specified by {@link #periodField},
 * which accepts one of the {@link Calendar} fields like {@link Calendar#MINUTE}.*/
public final class BackgroundThread {
	private static Thread thread;
	/** The tasks that are executed at the beginning of every period.*/
	private static Map<String, Runnable> tasks;
	/** One of the {@link Calendar} fields.*/
	private static int periodField;
	/** Records the starting time of every period. If the background thread is not currently
	 * running, it is -1.*/
	private static long startTimeMillis;
	/** Used decide whether the thread is exactly the thread we want.*/
	private static long currentThreadId;
	private static final Object LOCK = new Object();
	private BackgroundThread() {}
	/** Be sure to call it before using the class. */
	public static void init() {
		thread = new Thread();
		tasks = new HashMap<String, Runnable>();
		periodField = Calendar.MINUTE;
		currentThreadId = -1;
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
	/** Start the background thread. Do nothing if the thread is currently alive.*/
	public static void start() {
		if (isAlive()) return;
		thread = new Thread(() -> {
			// synchronize on a static lock to avoid multiple threads running at the same time
			synchronized (LOCK) {
				try {
					while (!Thread.interrupted()) {
						// update startTimeMillis
						startTimeMillis = CalendarUtils.setToEnd(System.currentTimeMillis(),
								periodField);
						// startTimeMillis may be modified, so back it up here
						long backup = startTimeMillis;
						// wait for some time
						long timeout = startTimeMillis - System.currentTimeMillis();
						LOCK.wait(timeout > 0 ? timeout : 1);
						// restore the backed up data
						startTimeMillis = backup;
						LogUtils.d("BackgroundThread",
								"startTimeMillis is " + startTimeMillis);
						// get the current thread and do some judges
						Thread currentThread = Thread.currentThread();
						if (currentThread.getId() == currentThreadId) {
							// run the tasks one by one
							LogUtils.d("BackgroundThread", "Running tasks: " +
									                               tasks.keySet().toString());
							for (Runnable runnable : tasks.values()) runnable.run();
						}
						else // if the thread is not correct, interrupt it
							currentThread.interrupt();
					}
				} catch (InterruptedException ignored) {}
				// when the thread is interrupted, come here
				startTimeMillis = -1;
			}
		});
		currentThreadId = thread.getId();
		thread.start();
	}
	@Contract(pure = true)
	public static boolean isAlive() {
		return startTimeMillis >= 0;
	}
	public static void interrupt() {
		thread.interrupt();
	}
	public static void clearTasks() {
		tasks.clear();
	}
	/** Clear the tasks of which the keys match the regexp.*/
	public static void clearTasks(String regexp) {
		Set<String> needRemoving = new ArraySet<String>();
		for (String key : tasks.keySet()) if (key.matches(regexp)) needRemoving.add(key);
		for (String key : needRemoving) removeTask(key);
	}
}
