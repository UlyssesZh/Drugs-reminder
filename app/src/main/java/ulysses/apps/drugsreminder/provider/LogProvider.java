package ulysses.apps.drugsreminder.provider;

import androidx.core.content.FileProvider;

import ulysses.apps.drugsreminder.BuildConfig;

public class LogProvider extends FileProvider {
	public static String AUTHORITY = BuildConfig.APPLICATION_ID + ".providers.LogProvider";
}
