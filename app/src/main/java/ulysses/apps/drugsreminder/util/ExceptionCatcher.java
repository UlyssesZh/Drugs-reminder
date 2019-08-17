package ulysses.apps.drugsreminder.util;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import ulysses.apps.drugsreminder.BuildConfig;

public class ExceptionCatcher {
	public static void catchException(Runnable runnable, OnExceptionCaughtListener listener) {
		if (BuildConfig.DEBUG)
			try {
				runnable.run();
			} catch (Exception e) {
				e.printStackTrace();
				listener.onExceptionCaught(e);
			}
		else runnable.run();
	}
	public static void catchException(Context context, Runnable runnable) {
		catchException(runnable, new ExceptionAlerter(context));
	}
	public interface OnExceptionCaughtListener {
		void onExceptionCaught(Exception e);
	}
	public static class ExceptionAlerter implements OnExceptionCaughtListener {
		private Context context;
		public ExceptionAlerter(Context context) {
			this.context = context;
		}
		@Override
		public void onExceptionCaught(Exception e) {
			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(e.getClass().getName());
			stringBuilder.append(": ");
			stringBuilder.append(e.getMessage());
			for (StackTraceElement stackTraceElement : e.getStackTrace()) {
				stringBuilder.append("\n");
				stringBuilder.append(stackTraceElement.toString());
			}
			alertBuilder.setMessage(stringBuilder.toString());
			alertBuilder.create().show();
		}
	}
	public static void printStackTrace() {
		for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
			if (element.getClassName().matches("ulysses.*"))
				Log.d("StackTrace", element.toString());
		}
	}
}
