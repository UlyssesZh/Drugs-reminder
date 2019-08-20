package ulysses.apps.drugsreminder.util;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/** @author Donkor */
public class PermissionPageUtils {
	private Context context;
	private PermissionPageUtils(Context context) {
		this.context = context;
	}
	private void goPermissionPage() {
		switch (Build.MANUFACTURER) {
			case "HUAWEI":
				goHuaWei();
				break;
			case "vivo":
				goVivo();
				break;
			case "OPPO":
				goOppo();
				break;
			case "Coolpad":
				goCoolpad();
				break;
			case "Meizu":
				goMeizu();
				break;
			case "Xiaomi":
				goXiaoMi();
				break;
			case "samsung":
				goSamsung();
				break;
			case "Sony":
				goSony();
				break;
			case "LG":
				goLG();
				break;
			default:
				goIntentSetting();
				break;
		}
	}
	private void goLG() {
		try {
			Intent intent = new Intent(Constants.packageName);
			ComponentName componentName = new ComponentName("com.android.settings",
					"com.android.settings.Settings$AccessLockSummaryActivity");
			intent.setComponent(componentName);
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			goIntentSetting();
		}
	}
	private void goSony() {
		try {
			Intent intent = new Intent(Constants.packageName);
			ComponentName componentName = new ComponentName("com.sonymobile.cta",
					"com.sonymobile.cta.SomcCTAMainActivity");
			intent.setComponent(componentName);
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			goIntentSetting();
		}
	}
	private void goHuaWei() {
		try {
			Intent intent = new Intent(Constants.packageName);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ComponentName componentName = new ComponentName("com.huawei.systemmanager",
					"com.huawei.permissionmanager.ui.MainActivity");
			intent.setComponent(componentName);
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			goIntentSetting();
		}
	}
	private void goXiaoMi() {
		Intent intent = new Intent();
		String version = getMiuiVersion();
		if (version == null) {
			goIntentSetting();
			return;
		}
		switch (version) {
			case "V6":
			case "V7":
				intent.setClassName("com.miui.securitycenter",
						"com.miui.permcenter.permissions.AppPermissionsEditorActivity");
				break;
			case "V8":
			case "V9":
			case "V10":
				intent.setClassName("com.miui.securitycenter",
						"com.miui.permcenter.permissions.PermissionsEditorActivity");
				break;
			default:
				goIntentSetting();
				return;
		}
		intent.setAction("miui.intent.action.APP_PERM_EDITOR");
		intent.putExtra("extra_pkgname", Constants.packageName);
		context.startActivity(intent);
	}
	private void goMeizu() {
		try {
			Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.putExtra("packageName", Constants.packageName);
			context.startActivity(intent);
		} catch (ActivityNotFoundException localActivityNotFoundException) {
			localActivityNotFoundException.printStackTrace();
			goIntentSetting();
		}
	}
	private void goSamsung() {
		goIntentSetting();
	}
	private void goIntentSetting() {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		Uri uri = Uri.fromParts("package", context.getPackageName(), null);
		intent.setData(uri);
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void goOppo() {
		doStartApplicationWithPackageName("com.coloros.safecenter");
	}
	private void goCoolpad() {
		doStartApplicationWithPackageName("com.yulong.android.security:remote");
	}
	private void goVivo() {
		doStartApplicationWithPackageName("com.bairenkeji.icaller");
	}
	private Intent getAppDetailSettingIntent() {
		Intent localIntent = new Intent();
		localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return localIntent;
	}
	private void doStartApplicationWithPackageName(String packagename) {
		PackageInfo packageinfo = null;
		try {
			packageinfo = context.getPackageManager().getPackageInfo(packagename, 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageinfo == null) return;
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageinfo.packageName);
		ResolveInfo resolveInfo = context.getPackageManager().queryIntentActivities(resolveIntent,
				0).iterator().next();
		if (resolveInfo == null) return;
		String packageName = resolveInfo.activityInfo.packageName;
		String className = resolveInfo.activityInfo.name;
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(new ComponentName(packageName, className));
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			goIntentSetting();
			e.printStackTrace();
		}
	}
	@Nullable
	private static String getMiuiVersion() {
		String result;
		BufferedReader input = null;
		try {
			input = new BufferedReader(new InputStreamReader(
					Runtime.getRuntime().exec("getprop ro.miui.ui.version.name")
							.getInputStream()), 1024);
			result = input.readLine();
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (input != null) input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	public static void goPermissionPage(Context context) {
		new PermissionPageUtils(context).goPermissionPage();
	}
}
