package ulysses.apps.drugsreminder.libraries;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ulysses.apps.drugsreminder.elements.Drug;
import ulysses.apps.drugsreminder.elements.Meal;
import ulysses.apps.drugsreminder.elements.Reminder;
import ulysses.apps.drugsreminder.preferences.Preferences;
import ulysses.apps.drugsreminder.util.BitmapCoder;
import ulysses.apps.drugsreminder.util.Time;

public final class ElementsLibrary {
	private static ArrayList<Drug> drugs = new ArrayList<Drug>();
	private static ArrayList<Reminder> reminders = new ArrayList<Reminder>();
	private static ArrayList<Meal> meals = new ArrayList<Meal>();
	private ElementsLibrary() {}
	@Contract(pure = true)
	public static Drug findDrugByID(int ID) {
		return drugs.get(ID);
	}
	public static void addDrug(@NotNull Drug drug) {
		int ID = drug.getID();
		extendDrugs(ID);
		drugs.set(ID, drug);
	}
	@Contract(pure = true)
	public static Reminder findReminderByID(int ID) {
		return reminders.get(ID);
	}
	public static void addReminder(@NotNull Reminder reminder) {
		int ID = reminder.getID();
		extendReminders(ID);
		reminders.set(ID, reminder);
	}
	@Contract(pure = true)
	public static Meal findMealByID(int ID) {
		return meals.get(ID);
	}
	public static void addMeal(@NotNull Meal meal) {
		int ID = meal.getID();
		extendMeals(ID);
		meals.set(ID, meal);
	}
	public static int drugsNumber() {
		return drugs.size();
	}
	public static int remindersNumber() {
		return reminders.size();
	}
	public static int mealsNumber() {
		return meals.size();
	}
	public static void deleteMeal(int ID) {
		extendMeals(ID);
		meals.set(ID, null);
	}
	public static void deleteReminder(int ID) {
		extendReminders(ID);
		reminders.set(ID, null);
	}
	public static void deleteDrug(int ID) {
		extendDrugs(ID);
		drugs.set(ID, null);
	}
	@Contract(pure = true)
	public static boolean doesNotHaveDrug(int ID) {
		return drugIDOutOfBound(ID) || drugs.get(ID) == null;
	}
	@Contract(pure = true)
	public static boolean doesNotHaveMeal(int ID) {
		return mealIDOutOfBound(ID) || meals.get(ID) == null;
	}
	@Contract(pure = true)
	public static boolean doesNotHaveReminder(int ID) {
		return reminderIDOutOfBound(ID) || reminders.get(ID) == null;
	}
	@Contract(pure = true)
	public static boolean doesNotHaveDrugs() {
		for (Drug drug : drugs)
			if (drug != null) return false;
		return true;
	}
	@Contract(pure = true)
	public static boolean doesNotHaveMeals() {
		for (Meal meal : meals)
			if (meal != null) return false;
		return true;
	}
	@Contract(pure = true)
	public static boolean doesNotHaveReminders() {
		for (Reminder reminder : reminders)
			if (reminder != null) return false;
		return true;
	}
	public static boolean drugIDOutOfBound(int ID) {
		return ID >= drugsNumber();
	}
	public static boolean mealIDOutOfBound(int ID) {
		return ID >= mealsNumber();
	}
	public static boolean reminderIDOutOfBound(int ID) {
		return ID >= remindersNumber();
	}
	private static void extendDrugs(int maxID) {
		while (drugIDOutOfBound(maxID)) drugs.add(null);
	}
	private static void extendMeals(int maxID) {
		while (mealIDOutOfBound(maxID)) meals.add(null);
	}
	private static void extendReminders(int maxID) {
		while (reminderIDOutOfBound(maxID)) reminders.add(null);
	}
	public static void saveMeal(int ID, @NotNull SharedPreferences.Editor editor) {
		String head = "meal" + ID;
		if (doesNotHaveMeal(ID))
			editor.putBoolean(head + "exists", false);
		else {
			editor.putBoolean(head + "exists", true);
			Meal meal = findMealByID(ID);
			editor.putString(head + "name", meal.getName());
			Time time = meal.getTime();
			editor.putInt(head + "hour", time.getHour());
			editor.putInt(head + "minute", time.getMinute());
		}
	}
	public static void saveMeals(@NotNull SharedPreferences.Editor editor) {
		editor.putInt("mealsNumber", mealsNumber());
		for (int ID = 0; !mealIDOutOfBound(ID); ID++)
			saveMeal(ID, editor);
	}
	public static void saveDrug(int ID, @NotNull SharedPreferences.Editor editor) {
		String head = "drug" + ID;
		if (doesNotHaveDrug(ID))
			editor.putBoolean(head + "exists", false);
		else {
			editor.putBoolean(head + "exists", true);
			Drug drug = findDrugByID(ID);
			editor.putString(head + "name", drug.getName());
			editor.putString(head + "bitmap", BitmapCoder.code(drug.getBitmap()));
		}
	}
	public static void saveDrugs(@NotNull SharedPreferences.Editor editor) {
		editor.putInt("drugsNumber", drugsNumber());
		for (int ID = 0; !drugIDOutOfBound(ID); ID++)
			saveDrug(ID, editor);
	}
	public static void saveReminder(int ID, @NotNull SharedPreferences.Editor editor) {
		String head = "reminder" + ID;
		if (doesNotHaveReminder(ID))
			editor.putBoolean(head + "exists", false);
		else {
			editor.putBoolean(head + "exists", true);
			Reminder reminder = findReminderByID(ID);
			Time relativeTime = reminder.getRelativeTime();
			editor.putInt(head + "hour", relativeTime.getHour());
			editor.putInt(head + "minute", relativeTime.getMinute());
			editor.putBoolean(head + "before", reminder.isBefore());
			editor.putStringSet(head + "drugIDs", codeList(reminder.getDrugIDs()));
			editor.putStringSet(head + "mealIDs", codeList(reminder.getMealIDs()));
			editor.putStringSet(head + "usageDosages", codeList(reminder.getUsageDosages()));
			editor.putInt(head + "repeatPeriod", reminder.getRepeatPeriod());
			editor.putLong(head + "createdTime", reminder.getCreatedTime());
			editor.putBoolean(head + "enabled", reminder.isEnabled());
			editor.putBoolean(head + "delayed", reminder.isDelayed());
		}
	}
	public static void saveReminders(@NotNull SharedPreferences.Editor editor) {
		editor.putInt("remindersNumber", remindersNumber());
		for (int ID = 0; !reminderIDOutOfBound(ID); ID++)
			saveReminder(ID, editor);
	}
	public static void saveElements(Context context) {
		SharedPreferences.Editor editor =
				context.getSharedPreferences("elements", Context.MODE_PRIVATE).edit();
		saveReminders(editor);
		saveDrugs(editor);
		saveMeals(editor);
		editor.apply();
	}
	public static void loadMeal(int ID, @NotNull SharedPreferences preferences) {
		String head = "meal" + ID;
		if (preferences.getBoolean(head + "exists", false))
			addMeal(new Meal(ID, preferences.getString(head + "name", ""),
					new Time(preferences.getInt(head + "hour", 0),
							preferences.getInt(head + "minute", 0))));
		else deleteMeal(ID);
	}
	public static void loadMeals(@NotNull SharedPreferences preferences) {
		extendMeals(preferences.getInt("mealsNumber", 0) - 1);
		for (int ID = 0; !mealIDOutOfBound(ID); ID++)
			loadMeal(ID, preferences);
	}
	public static void loadDrug(int ID, @NotNull SharedPreferences preferences) {
		String head = "drug" + ID;
		if (preferences.getBoolean(head + "exists", false))
			addDrug(new Drug(ID, preferences.getString(head + "name", ""),
					BitmapCoder.decode(preferences.getString(head + "bitmap", ""))));
		else deleteDrug(ID);
	}
	public static void loadDrugs(@NotNull SharedPreferences preferences) {
		extendDrugs(preferences.getInt("drugsNumber", 0) - 1);
		for (int ID = 0; !drugIDOutOfBound(ID); ID++)
			loadDrug(ID, preferences);
	}
	public static void loadReminder(int ID, @NotNull SharedPreferences preferences) {
		String head = "reminder" + ID;
		if (preferences.getBoolean(head + "exists", false)) {
			Reminder reminder = new Reminder(ID, decodeStringSet(preferences.getStringSet(
					head + "mealIDs", new LinkedHashSet<String>(0))),
					preferences.getBoolean(head + "before", true),
					new Time(preferences.getInt(head + "hour", 0),
							preferences.getInt(head + "minute", 0)),
					decodeStringSet(preferences.getStringSet(head + "drugIDs",
							new LinkedHashSet<String>(0))),
					new ArrayList<String>(preferences.getStringSet(head + "usageDosages",
							new LinkedHashSet<String>(0))),
					preferences.getInt(head + "repeatPeriod", 1),
					preferences.getLong(head + "createdTime",
							System.currentTimeMillis()));
			reminder.setEnabled(preferences.getBoolean(head + "enabled", true));
			reminder.setDelayed(preferences.getBoolean(head + "delayed", false));
			addReminder(reminder);
		} else deleteReminder(ID);
	}
	public static void loadReminders(@NotNull SharedPreferences preferences) {
		extendReminders(preferences.getInt("remindersNumber", 0) - 1);
		for (int ID = 0; !reminderIDOutOfBound(ID); ID++)
			loadReminder(ID, preferences);
	}
	public static void loadElements(@NotNull Context context) {
		SharedPreferences preferences = context.getSharedPreferences("elements",
				Context.MODE_PRIVATE);
		loadReminders(preferences);
		loadDrugs(preferences);
		loadMeals(preferences);
	}
	private static Set<String> codeList(@NotNull List list) {
		Set<String> result = new LinkedHashSet<String>(list.size());
		for (Object object : list)
			result.add(object.toString());
		return result;
	}
	private static List<Integer> decodeStringSet(@NotNull Set<String> code) {
		List<Integer> result = new ArrayList<Integer>(code.size());
		for (String string : code)
			result.add(Integer.decode(string));
		return result;
	}
}
