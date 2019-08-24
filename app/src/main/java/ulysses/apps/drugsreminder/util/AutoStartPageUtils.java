package ulysses.apps.drugsreminder.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import ulysses.apps.drugsreminder.R;

public final class AutoStartPageUtils {
	public static void goAutoStartPage(Context context){
		String packageName = null;
		String className = null;
		switch (Build.BRAND.toLowerCase()) {
			case "samsung":
				packageName = "com.samsung.android.sm";
				className = "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity";
				break;
			case "huawei":
				packageName = "com.huawei.systemmanager";
				className = "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity";
				break;
			case "xiaomi":
				packageName = "com.miui.securitycenter";
				className = "com.miui.permcenter.autostart.AutoStartManagementActivity";
				break;
			case "vivo":
				packageName = "com.iqoo.secure";
				className = "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity";
				break;
			case "oppo":
				packageName = "com.coloros.oppoguardelf";
				className = "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity";
				break;
			case "360":
				packageName = "com.yulong.android.coolsafe";
				className = "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity";
				break;
			case "meizu":
				packageName = "com.meizu.safe";
				className = "com.meizu.safe.permission.SmartBGActivity";
				break;
			case "oneplus":
				packageName = "com.oneplus.security";
				className = "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity";
				break;
		}
		if (packageName == null) {
			Toast.makeText(context, R.string.neednt_auto_start_hint,
					Toast.LENGTH_LONG).show();
		} else {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				intent.setComponent(new ComponentName(packageName, className));
				context.startActivity(intent);
			} catch (Exception e) {
				intent.setAction(Settings.ACTION_SETTINGS);
				context.startActivity(intent);
			}
		}
	}
}
