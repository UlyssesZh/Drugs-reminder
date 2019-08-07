package ulysses.apps.drugsreminder.activities;

import android.view.View;
import android.widget.Button;
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
	private TextView mealsEmpty;
	private LinearLayout editMealsCheckboxes;
	private ScrollDisabledListView editDrugs;
	private Button editAddDrug;
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
		mealsEmpty = findViewById(R.id.edit_reminder_meals_empty);
		editMealsCheckboxes = findViewById(R.id.edit_reminder_meals_checkboxes);
		editDrugs = findViewById(R.id.edit_reminder_drugs);
		editAddDrug = findViewById(R.id.edit_reminder_add_drug);
		editRepeatPeriod = findViewById(R.id.edit_reminder_frequency);
		editStartingDay = findViewById(R.id.edit_reminder_starting_day);
		mealIDs = new ArrayList<Integer>();
		checkedMealIDs = new ArrayList<Integer>();
		editRelativeTime.setIs24HourView(true);
		editRelativeTime.setHour(0);
		editRelativeTime.setMinute(0);
		editRelativeTime.setOnTimeChangedListener((view, hourOfDay, minute) -> refreshTime());
		editBeforeAfter.setOnCheckedChangeListener((group, checkedId) -> refreshTime());
		if (!ElementsLibrary.doesNotHaveMeals()) {
			mealsEmpty.setVisibility(View.INVISIBLE);
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
		editAddDrug.setOnClickListener(view -> {
			if (drugListItems == null)
				drugListItems = new ArrayList<Map<String, Object>>();
			if (drugIDs == null)
				drugIDs = new ArrayList<Integer>();
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
						new int[]{R.id.drug_name, R.id.drug_image}), (dialog, which) -> {
					int addDrugID = addDrugIDs.get(which);
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
	protected void loadViews(Reminder reminder) {
		Time time = reminder.getRelativeTime();
		editRelativeTime.setHour(time.getHour());
		editRelativeTime.setMinute(time.getMinute());
		editBeforeAfter.check(reminder.isBefore() ? R.id.edit_reminder_before :
				                      R.id.edit_reminder_after);
		for (int i = 0; i < mealIDs.size(); i++) {
			int mealID = mealIDs.get(i);
			if (reminder.getMealIDs().contains(mealID))
				((CheckBox) editMealsCheckboxes.getChildAt(i)).setChecked(true);
		}
		drugIDs = reminder.getDrugIDs();
		drugListItems = new ArrayList<Map<String, Object>>(drugsNumber());
		for (int i = 0; i < drugsNumber(); i++)
			addDrugListItem(ElementsLibrary.findDrugByID(drugIDs.get(i)),
					reminder.getUsageDosages().get(i));
		editRepeatPeriod.setText(String.valueOf(reminder.getRepeatPeriod()));
		refreshDrugList();
		if (pickStartingTime()) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(reminder.getCreatedTime());
			editStartingDay.init(calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH),
					(view, year, monthOfYear, dayOfMonth) -> {});
		}
	}
	@Override
	protected void deleteElement(int ID) {
		ElementsLibrary.deleteReminder(ID);
	}
	@Override
	protected boolean saveChanges(int ID) {
		if (editBeforeAfter.getCheckedRadioButtonId() == -1) {
			alert(R.string.before_after_empty_hint);
			return false;
		} else if (checkedMealIDs.isEmpty()) {
			alert(R.string.meals_empty_hint);
			return false;
		} else if (drugListItems == null || drugListItems.isEmpty()) {
			alert(R.string.drugs_empty_hint);
			return false;
		} else {
			loadUsageDosage();
			List<String> usageDosages = new ArrayList<String>(drugsNumber());
			for (int i = 0; i < drugsNumber(); i++)
				usageDosages.add((String) drugListItems.get(i).get("usage_dosage"));
			int repeatPeriod;
			try {
				repeatPeriod = Integer.decode(editRepeatPeriod.getText().toString());
			} catch (NumberFormatException e) {
				repeatPeriod = 1;
			}
			if (repeatPeriod == 0) repeatPeriod = 1;
			long createdTime;
			if (pickStartingTime()) {
				Calendar calendar = Calendar.getInstance();
				calendar.set(editStartingDay.getYear(), editStartingDay.getMonth(),
						editStartingDay.getDayOfMonth(), 0, 0, 0);
				createdTime = calendar.getTimeInMillis();
			} else if (isNotCreating(ID))
				createdTime = getElement(ID).getCreatedTime();
			else
				createdTime = System.currentTimeMillis();
			ElementsLibrary.addReminder(new Reminder(ID, checkedMealIDs,
					editBeforeAfter.getCheckedRadioButtonId() == R.id.edit_reminder_before,
					relativeTime(), drugIDs, usageDosages, repeatPeriod, createdTime));
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
		if (editBeforeAfter.getCheckedRadioButtonId() != -1 && checkedMealIDs.size() > 0)
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
}
