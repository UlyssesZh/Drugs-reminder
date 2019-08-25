package ulysses.apps.drugsreminder.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ulysses.apps.drugsreminder.BuildConfig;
import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.provider.LogProvider;

public final class LogUtils {
	private static final String OUTPUT_FILENAME = "log";
	private static final String OUTPUT_FILE_EXT = "txt";
	private static String filePath;
	private static int priorityFilter = Log.VERBOSE;
	public static void init(Context context) {
		initFilePath(context);
	}
	private static void initFilePath(Context context) {
		String dirPath;
		try {
			dirPath = getDirPath(context);
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
			dirPath = getDirPath();
		}
		filePath = getFilePath(dirPath);
	}
	private static void initFilePath() {
		filePath = getFilePath(getDirPath());
	}
	@NotNull
	private static String getDirPath(@NotNull Context context) throws IOException {
		return context.getExternalFilesDir(null).getCanonicalPath();
	}
	@NotNull
	private static String getDirPath() {
		return String.format("%sAndroid/data/%s/files",
				Environment.getExternalStorageDirectory(), BuildConfig.APPLICATION_ID);
	}
	@NotNull
	private static String getFilePath(String dirPath) {
		return String.format("%s/%s.%s", dirPath, OUTPUT_FILENAME, OUTPUT_FILE_EXT);
	}
	private static void writeLog(int priority, String type, String tag, String message) {
		if (priority >= priorityFilter)
			appendLog(String.format("%s %s/%s: %s",
					CalendarUtils.format(System.currentTimeMillis()), type, tag, message));
	}
	private static void writeLog(int priority, String type, String tag, String message,
	                             @NotNull Throwable throwable) {
		if (priority >= priorityFilter) {
			appendLog(String.format("%s %s/%s: %s",
					CalendarUtils.format(System.currentTimeMillis()), type, tag, message));
			for (StackTraceElement element : throwable.getStackTrace())
				appendLog(element.toString());
		}
	}
	public static void i(String tag, String message) {
		Log.i(tag, message);
		writeLog(Log.INFO, "I", tag, message);
	}
	public static void i(String tag, String message, Throwable throwable) {
		Log.i(tag, message, throwable);
		writeLog(Log.INFO, "I", tag, message, throwable);
	}
	public static void d(String tag, String message) {
		Log.d(tag, message);
		writeLog(Log.DEBUG, "D", tag, message);
	}
	public static void d(String tag, String message, Throwable throwable) {
		Log.d(tag, message, throwable);
		writeLog(Log.DEBUG, "D", tag, message, throwable);
	}
	public static void e(String tag, String message) {
		Log.e(tag, message);
		writeLog(Log.ERROR, "E", tag, message);
	}
	public static void e(String tag, String message, Throwable throwable) {
		Log.e(tag, message, throwable);
		writeLog(Log.ERROR, "E", tag, message, throwable);
	}
	public static void w(String tag, String message) {
		Log.w(tag, message);
		writeLog(Log.WARN, "W", tag, message);
	}
	public static void w(String tag, String message, Throwable throwable) {
		Log.w(tag, message, throwable);
		writeLog(Log.WARN, "W", tag, message, throwable);
	}
	public static void v(String tag, String message) {
		Log.v(tag, message);
		writeLog(Log.VERBOSE, "V", tag, message);
	}
	public static void v(String tag, String message, Throwable throwable) {
		Log.v(tag, message, throwable);
		writeLog(Log.VERBOSE, "V", tag, message, throwable);
	}
	public static void clear(Context context) {
		if (filePath == null) initFilePath(context);
		File file = new File(filePath);
		if (file.exists()) file.delete();
		Toast.makeText(context, R.string.clear_log_succeeded_hint, Toast.LENGTH_LONG)
				.show();
	}
	public static void openLog(Context context) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			if (filePath == null) initFilePath(context);
			File file = new File(filePath);
			Uri uri = FileProvider.getUriForFile(context, LogProvider.AUTHORITY, file);
			intent.setDataAndType(uri, "text/plain");
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
					                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
					                Intent.FLAG_ACTIVITY_NEW_TASK);
			if (intent.resolveActivity(context.getPackageManager()) == null)
				Toast.makeText(context, R.string.open_log_failed_hint,
						Toast.LENGTH_LONG).show();
			else context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, R.string.open_log_failed_hint,
					Toast.LENGTH_LONG).show();
		}
	}
	private static void appendLog(String text) {
		try {
			if (filePath == null) initFilePath();
			File file = new File(filePath);
			if (!file.exists()) file.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			writer.append(text);
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Contract(pure = true)
	public static String getFilePath() {
		return filePath;
	}
	public static void setFilePath(String filePath) {
		LogUtils.filePath = filePath;
	}
	@Contract(pure = true)
	public static int getPriorityFilter() {
		return priorityFilter;
	}
	public static void setPriorityFilter(int priorityFilter) {
		LogUtils.priorityFilter = priorityFilter;
	}
}
