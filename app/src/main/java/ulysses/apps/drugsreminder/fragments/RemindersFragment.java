package ulysses.apps.drugsreminder.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.CompoundButton;

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
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				super.handleMessage(message);
				if (getContext() != null && message.what == MESSAGE_WHAT)
					try {
						refresh();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
			}
		};
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
	protected int layoutFile() {
		return R.layout.reminders_fragment;
	}
	@Override
	protected int dataLayoutFile() {
		return R.layout.reminder_item;
	}
	@Override
	protected String[] fromStrings() {
		return new String[]{"drugs", "time", "next_time", "enabled"};
	}
	@Override
	protected int[] toStrings() {
		return new int[]{R.id.reminder_drugs, R.id.reminder_time, R.id.reminder_next_time,
				R.id.reminder_switch};
	}
	@Override
	protected Object findContentFromStringIndex(Reminder reminder, int stringIndex) {
		switch (stringIndex) {
			case 0:
				StringBuilder drugsStringBuilder = new StringBuilder();
				List<Integer> drugIDs = reminder.getDrugIDs();
				int iMax = drugIDs.size() - 1;
				for (int i = 0; i <= iMax; i++) {
					drugsStringBuilder.append(ElementsLibrary.findDrugByID(drugIDs.get(i)).getName());
					if (i != iMax) drugsStringBuilder.append(getString(R.string.comma));
				}
				return drugsStringBuilder.toString();
			case 1:
				return reminder.timeString(getResources());
			case 2:
				return getString(R.string.reminder_next_time_format,
						reminder.nextTimeString(getResources()));
			case 3:
				return new Object[] {reminder.isEnabled(),
						(CompoundButton.OnCheckedChangeListener) (buttonView, isChecked) -> {
							reminder.setEnabled(isChecked);
							Context context = getContext();
							if (context == null) return;
							ElementsLibrary.saveElements(context);
							AlarmsLibrary.setupAlarms(context, reminder.getID());
						}};
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
}
