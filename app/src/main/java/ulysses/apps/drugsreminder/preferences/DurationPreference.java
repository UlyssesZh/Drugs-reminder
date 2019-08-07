package ulysses.apps.drugsreminder.preferences;

import android.app.TimePickerDialog;
import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.Preference;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.util.Time;

public class DurationPreference extends Preference {
	public DurationPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	public DurationPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	public DurationPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public DurationPreference(Context context) {
		super(context);
	}
	@Override
	protected void onClick() {
		super.onClick();
		Time advanceTime = Preferences.reminderAdvanceTime;
		new TimePickerDialog(getContext(), R.style.AppTheme_TimePickerWithSpinner,
				(view, hourOfDay, minute) -> {
					Time newTime = new Time(hourOfDay, minute);
					if (callChangeListener(newTime)) persistInt(hourOfDay * 60 + minute);
				}, advanceTime.getHour(), advanceTime.getMinute(),
				true).show();
	}
}
