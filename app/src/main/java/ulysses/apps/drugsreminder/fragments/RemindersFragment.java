package ulysses.apps.drugsreminder.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ulysses.apps.drugsreminder.activities.EditReminderActivity;
import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.libraries.AlarmsLibrary;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.elements.Reminder;

public class RemindersFragment extends ElementsFragment<Reminder> {
	private static final int MESSAGE_WHAT = 0x0520;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Handler handler = new Refresher(this);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(MESSAGE_WHAT);
			}
		}, (60 - Calendar.getInstance().get(Calendar.SECOND)) * 1000, 60000);
	}
	@Override
	protected int listSize() {
		return ElementsLibrary.remindersNumber();
	}
	@Override
	protected int layoutId() {
		return R.layout.reminders_fragment;
	}
	@Override
	protected int listItemLayoutId() {
		return R.layout.reminder_item;
	}
	@Override
	protected String[] from() {
		return new String[]{"drugs", "time", "next_time", "enabled"};
	}
	@Override
	protected int[] to() {
		return new int[]{R.id.reminder_drugs, R.id.reminder_time, R.id.reminder_next_time,
				R.id.reminder_switch};
	}
	@Override
	protected Object findContentFromIndex(Reminder reminder, int index) {
		switch (index) {
			case 0:
				return dataForCheckable(reminder, reminder.drugsString(getResources()));
			case 1:
				return dataForCheckable(reminder, reminder.timeString(getResources()));
			case 2:
				return dataForCheckable(reminder, getString(R.string.reminder_next_time_format,
						reminder.nextTimeString(getResources())));
			case 3:
				return dataForCheckable(reminder,
						(CompoundButton.OnCheckedChangeListener) (buttonView, isChecked) -> {
							if (reminder.isEnabled() == isChecked) return;
							reminder.setEnabled(isChecked);
							ViewParent viewParent = buttonView.getParent();
							if (viewParent instanceof View)
								for (int viewId : to()) {
									View view = ((View) viewParent).findViewById(viewId);
									if (view instanceof CheckedTextView)
										((CheckedTextView) view).setChecked(isChecked);
								}
							Context context = getContext();
							if (context == null) return;
							ElementsLibrary.saveElements(context);
							AlarmsLibrary.setupAllAlarms(context);
						});
		}
		return null;
	}
	@Override
	protected boolean doesNotHave(int ID) {
		return ElementsLibrary.doesNotHaveReminder(ID);
	}
	@Override
	protected Class<EditReminderActivity> editActivityClass() {
		return EditReminderActivity.class;
	}
	@Override
	protected boolean isEmpty() {
		return ElementsLibrary.doesNotHaveReminders();
	}
	@Override
	protected Reminder getElement(int ID) {
		return ElementsLibrary.findReminderByID(ID);
	}
	private Object[] dataForCheckable(Reminder reminder, Object data) {
		return new Object[] {reminder.isEnabled(), data};
	}
	private static class Refresher extends Handler {
		ElementsFragment<Reminder> fragment;
		private Refresher(ElementsFragment<Reminder> fragment) {
			this.fragment = fragment;
		}
		@Override
		public void handleMessage(@NonNull Message message) {
			super.handleMessage(message);
			if (fragment.getContext() != null && message.what == MESSAGE_WHAT)
				try {
					fragment.refresh();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
		}
	}
}
