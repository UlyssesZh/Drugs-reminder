package ulysses.apps.drugsreminder.elements;

import android.content.res.Resources;

import java.util.List;

import ulysses.apps.drugsreminder.util.Time;

public interface IReminder extends IElement {
	boolean isRepeating();
	List<Integer> getDrugIDs();
	List<String> getUsageDosages();
	boolean isEnabled();
	List<Long> alarmTimeMillis();
	String nextTimeString(Resources resources);
	int getRepeatPeriod();
	String timeString(Resources resources);
	String drugsString(Resources resources);
}
