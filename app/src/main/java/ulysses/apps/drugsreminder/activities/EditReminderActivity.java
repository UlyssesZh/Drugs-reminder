package ulysses.apps.drugsreminder.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.elements.Drug;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.elements.Reminder;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.views.ScrollDisabledListView;
import ulysses.apps.drugsreminder.adapters.ImprovedSimpleAdapter;
import ulysses.apps.drugsreminder.util.Time;

public class EditReminderActivity extends EditElementActivity<Reminder> {
	private TextView editTime;
	private TimePicker editRelativeTime;
	private RadioGroup editBeforeAfter;
	private LinearLayout editMealsCheckboxes;
	private ScrollDisabledListView editDrugs;
	private List<Integer> mealIDs;
	private List<Integer> checkedMealIDs;
	private List<Integer> drugIDs;
	private EditText editRepeatPeriod;
	private DatePicker editStartingDay;
	private List<Map<String, Object>> drugListItems;
	@Override
	protected int layoutFile() {
		return R.layout.edit_reminder_activity;
	}
	@Override
	protected void setupViews() {
		editTime = findViewById(R.id.edit_reminder_time);
		editRelativeTime = findViewById(R.id.edit_reminder_relative_time);
		editBeforeAfter = findViewById(R.id.edit_reminder_before_after);
		editMealsCheckboxes = findViewById(R.id.edit_reminder_meals_checkboxes);
		editDrugs = findViewById(R.id.edit_reminder_drugs);
		editRepeatPeriod = findViewById(R.id.edit_reminder_frequency);
		editStartingDay = findViewById(R.id.edit_reminder_starting_day);
		mealIDs = new ArrayList<Integer>();
		checkedMealIDs = new ArrayList<Integer>();
		drugListItems = new ArrayList<Map<String, Object>>();
		drugIDs = new ArrayList<Integer>();
		editRelativeTime.setIs24HourView(true);
		editRelativeTime.setOnTimeChangedListener((view, hourOfDay, minute) -> refreshTime());
		editBeforeAfter.setOnCheckedChangeListener((group, checkedId) -> refreshTime());
		if (!ElementsLibrary.doesNotHaveMeals()) {
			findViewById(R.id.edit_reminder_meals_empty).setVisibility(View.INVISIBLE);
			for (int mealID = 0; mealID < ElementsLibrary.mealsNumber(); mealID++) {
				if (ElementsLibrary.doesNotHaveMeal(mealID)) continue;
				mealIDs.add(mealID);
				final CheckBox checkBox = new CheckBox(this);
				checkBox.setText(ElementsLibrary.findMealByID(mealID).getName());
				final int mealIDInLambda = mealID;
				checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
					if (isChecked)
						checkedMealIDs.add(mealIDInLambda);
					else
						checkedMealIDs.remove((Integer) mealIDInLambda);
					refreshTime();
				});
				editMealsCheckboxes.addView(checkBox);
			}
		}
		findViewById(R.id.edit_reminder_add_drug).setOnClickListener(view -> {
			int addDrugsNumber = ElementsLibrary.drugsNumber() - drugListItems.size();
			if (addDrugsNumber == 0)
				alert(R.string.empty_hint);
			else {
				List<Map<String, Object>> addDrugItems = new ArrayList<Map<String, Object>>(addDrugsNumber);
				List<Integer> addDrugIDs = new ArrayList<Integer>(addDrugsNumber);
				for (int addDrugID = 0; addDrugID < ElementsLibrary.drugsNumber(); addDrugID++) {
					if (ElementsLibrary.doesNotHaveDrug(addDrugID) || drugIDs.contains(addDrugID))
						continue;
					Drug drug = ElementsLibrary.findDrugByID(addDrugID);
					addDrugIDs.add(addDrugID);
					Map<String, Object> addDrugItem = new HashMap<String, Object>(2);
					addDrugItem.put("name", drug.getName());
					addDrugItem.put("image", drug.getBitmap());
					addDrugItems.add(addDrugItem);
				}
				alert(new ImprovedSimpleAdapter(this, addDrugItems,
						R.layout.drug_item, new String[]{"name", "image"},
						new int[]{R.id.drug_name, R.id.drug_image}), (dialogInterface, i) -> {
					int addDrugID = addDrugIDs.get(i);
					drugIDs.add(addDrugID);
					addDrugListItem(ElementsLibrary.findDrugByID(addDrugID), "");
					refreshDrugList();
				});
			}
		});
		if (!pickStartingTime())
			findViewById(R.id.edit_reminder_starting_day_row).setVisibility(View.GONE);
	}
	@Override
	protected void loadViews() {
		editRelativeTime.setHour(0);
		editRelativeTime.setMinute(0);
		editRepeatPeriod.setText(String.valueOf(Preferences.defaultFrequency));
	}
	@Override
	protected void loadViews(Reminder reminder) {
		Time time = reminder.getRelativeTime();
		editRelativeTime.setHour(time.getHour());
		editRelativeTime.setMinute(time.getMinute());
		editBeforeAfter.check(reminder.isBefore() ? R.id.edit_reminder_before :
				                      R.id.edit_reminder_after);
		checkMealIDs(reminder.getMealIDs());
		drugIDs = reminder.getDrugIDs();
		addAllDrugs(reminder.getUsageDosages());
		editRepeatPeriod.setText(String.valueOf(reminder.getRepeatPeriod()));
		if (pickStartingTime()) setWallTimeToDatePicker(reminder.getCreatedTime());
	}
	@Override
	protected void loadViews(Bundle savedInstanceState) {
		editRelativeTime.setHour(savedInstanceState.getInt("hour"));
		editRelativeTime.setMinute(savedInstanceState.getInt("minute"));
		editBeforeAfter.check(savedInstanceState.getBoolean("before") ?
				                      R.id.edit_reminder_before : R.id.edit_reminder_after);
		checkMealIDs(savedInstanceState.getIntegerArrayList("mealIDs"));
		drugIDs = savedInstanceState.getIntegerArrayList("drugIDs");
		addAllDrugs(savedInstanceState.getStringArrayList("usageDosages"));
		editRepeatPeriod.setText(savedInstanceState.getString("repeatPeriod"));
		long createdTime = savedInstanceState.getLong("createdTime");
		if (pickStartingTime() && createdTime != 0)
			setWallTimeToDatePicker(createdTime);
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("hour", editRelativeTime.getHour());
		outState.putInt("minute", editRelativeTime.getMinute());
		outState.putBoolean("before",
				editBeforeAfter.getCheckedRadioButtonId() == R.id.edit_reminder_before);
		outState.putIntegerArrayList("mealIDs", new ArrayList<Integer>(checkedMealIDs));
		outState.putIntegerArrayList("drugIDs", new ArrayList<Integer>(drugIDs));
		outState.putStringArrayList("usageDosages", new ArrayList<String>(getUsageDosages()));
		outState.putString("repeatPeriod", editRepeatPeriod.getText().toString());
		outState.putLong("createdTime", getWallTimeFromDatePicker());
	}
	@Override
	protected void deleteElement(int ID) {
		ElementsLibrary.deleteReminder(ID);
	}
	@Override
	protected boolean saveChanges(int ID) {
		if (drugListItems == null || drugListItems.isEmpty()) {
			alert(R.string.drugs_empty_hint);
			return false;
		} else if (!relativeTime().isZero() && editBeforeAfter.getCheckedRadioButtonId() == -1) {
			alert(R.string.before_after_empty_hint);
			return false;
		} else if (checkedMealIDs.isEmpty()) {
			alert(R.string.meals_empty_hint);
			return false;
		} else {
			int repeatPeriod;
			try {
				repeatPeriod = Integer.decode(editRepeatPeriod.getText().toString());
			} catch (NumberFormatException e) {
				repeatPeriod = Preferences.defaultFrequency;
			}
			if (repeatPeriod <= 0) repeatPeriod = Preferences.defaultFrequency;
			ElementsLibrary.addReminder(new Reminder(ID, checkedMealIDs,
					editBeforeAfter.getCheckedRadioButtonId() == R.id.edit_reminder_before,
					relativeTime(), drugIDs, getUsageDosages(), repeatPeriod, getCreatedTime(ID)));
			return true;
		}
	}
	@Override
	protected boolean isNotCreating(int ID) {
		return !ElementsLibrary.reminderIDOutOfBound(ID);
	}
	private void refreshDrugList() {
		loadUsageDosage();
		editDrugs.setAdapter(new ImprovedSimpleAdapter(this, drugListItems,
				R.layout.edit_reminder_drug_item, new String[]{"name", "image", "usage_dosage"},
				new int[]{R.id.edit_reminder_drug_name, R.id.edit_reminder_drug_image,
						R.id.edit_reminder_drug_usage_dosage}));
		editDrugs.post(() -> {
			for (int i = 0; i < drugsNumber(); i++) {
				final int position = i;
				getDrugView(position, R.id.edit_reminder_delete_drug)
						.setOnClickListener(view -> {
							removeDrugListItem(position);
							refreshDrugList();
						});
			}
		});
	}
	private void addDrugListItem(@NotNull Drug drug, String usageDosage) {
		Map<String, Object> drugListItem = new HashMap<String, Object>(3);
		drugListItem.put("name", drug.getName());
		drugListItem.put("image", drug.getBitmap());
		drugListItem.put("usage_dosage", usageDosage);
		drugListItems.add(drugListItem);
	}
	private void removeDrugListItem(int index) {
		drugIDs.remove(index);
		drugListItems.remove(index);
	}
	private View getDrugView(int position, int viewID) {
		return editDrugs.getChildAt(position - editDrugs.getFirstVisiblePosition())
				       .findViewById(viewID);
	}
	private void loadUsageDosage() {
		if (editDrugs.getAdapter() != null) {
			int a = editDrugs.getChildCount();
			int b = drugIDs.size();
			int iBound = a > b ? b : a;
			for (int i = 0; i < iBound; i++)
				drugListItems.get(i).put("usage_dosage", ((EditText) getDrugView(i,
						R.id.edit_reminder_drug_usage_dosage)).getText().toString());
		}
	}
	private int drugsNumber() {
		return drugIDs.size();
	}
	private void refreshTime() {
		if ((relativeTime().isZero() || editBeforeAfter.getCheckedRadioButtonId() != -1) &&
				    checkedMealIDs.size() > 0)
			editTime.setText(Reminder.timeString(checkedMealIDs, relativeTime(),
					editBeforeAfter.getCheckedRadioButtonId() == R.id.edit_reminder_before,
					getResources()));
		else
			editTime.setText("");
	}
	@NotNull
	@Contract(" -> new")
	private Time relativeTime() {
		return new Time(editRelativeTime);
	}
	@Contract(pure = true)
	private boolean pickStartingTime() {
		return Preferences.startingTimeType.equals(Preferences.STARTING_TIME_TYPE_PICK);
	}
	@Override
	protected Reminder getElement(int ID) {
		return ElementsLibrary.findReminderByID(ID);
	}
	private long getCreatedTime(int ID) {
		if (pickStartingTime())
			return getWallTimeFromDatePicker();
		else if (isNotCreating(ID))
			return getElement(ID).getCreatedTime();
		else
			return System.currentTimeMillis();
	}
	private long getWallTimeFromDatePicker() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(editStartingDay.getYear(), editStartingDay.getMonth(),
				editStartingDay.getDayOfMonth(), 0, 0, 0);
		return calendar.getTimeInMillis();
	}
	private void setWallTimeToDatePicker(long timeMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		editStartingDay.init(calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH),
				(view, year, monthOfYear, dayOfMonth) -> {});
	}
	private void checkMealIDs(List<Integer> mealIDsForChecking) {
		for (int i = 0; i < mealIDs.size(); i++) {
			int mealID = mealIDs.get(i);
			if (mealIDsForChecking.contains(mealID))
				((CheckBox) editMealsCheckboxes.getChildAt(i)).setChecked(true);
		}
	}
	private void addAllDrugs(List<String> usageDosages) {
		drugListItems.clear();
		for (int i = 0; i < drugsNumber(); i++)
			addDrugListItem(ElementsLibrary.findDrugByID(drugIDs.get(i)),
					usageDosages.get(i));
		refreshDrugList();
	}
	private List<String> getUsageDosages() {
		loadUsageDosage();
		List<String> result = new ArrayList<String>(drugsNumber());
		for (int i = 0; i < drugsNumber(); i++)
			result.add((String) drugListItems.get(i).get("usage_dosage"));
		return result;
	}
}
