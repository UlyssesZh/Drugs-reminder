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

import ulysses.apps.drugsreminder.elements.DelayedReminder;
import ulysses.apps.drugsreminder.elements.Drug;
import ulysses.apps.drugsreminder.elements.IReminder;
import ulysses.apps.drugsreminder.elements.Meal;
import ulysses.apps.drugsreminder.elements.Reminder;
import ulysses.apps.drugsreminder.util.BitmapCoder;
import ulysses.apps.drugsreminder.util.Time;

public final class ElementsLibrary {
	private static ArrayList<Drug> drugs = new ArrayList<Drug>();
	private static ArrayList<IReminder> reminders = new ArrayList<IReminder>();
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
	public static IReminder findReminderByID(int ID) {
		return reminders.get(ID);
	}
	public static void addReminder(@NotNull IReminder reminder) {
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
		for (IReminder reminder : reminders)
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
	private static void saveMeal(int ID, @NotNull SharedPreferences.Editor editor) {
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
	private static void saveMeals(@NotNull SharedPreferences.Editor editor) {
		editor.putInt("mealsNumber", mealsNumber());
		for (int ID = 0; !mealIDOutOfBound(ID); ID++)
			saveMeal(ID, editor);
	}
	private static void saveDrug(int ID, @NotNull SharedPreferences.Editor editor) {
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
	private static void saveDrugs(@NotNull SharedPreferences.Editor editor) {
		editor.putInt("drugsNumber", drugsNumber());
		for (int ID = 0; !drugIDOutOfBound(ID); ID++)
			saveDrug(ID, editor);
	}
	private static void saveReminder(int ID, @NotNull SharedPreferences.Editor editor) {
		String head = "reminder" + ID;
		if (doesNotHaveReminder(ID))
			editor.putBoolean(head + "exists", false);
		else {
			editor.putBoolean(head + "exists", true);
			IReminder reminder = findReminderByID(ID);
			if (reminder.isRepeating()) {
				Reminder aReminder = (Reminder) reminder;
				editor.putBoolean(head + "delayed", false);
				editor.putInt(head + "repeatPeriod", aReminder.getRepeatPeriod());
				editor.putLong(head + "createdTime", aReminder.getCreatedTime());
				editor.putBoolean(head + "enabled", aReminder.isEnabled());
				// save relativeTime
				editor.putInt(head + "relativeMinutes", aReminder.getRelativeTime().minutes());
				// save before / after
				editor.putBoolean(head + "before", aReminder.isBefore());
				// save drugIDs and usageDosages
				List<Integer> drugIDs = aReminder.getDrugIDs();
				List<String> usageDosages = aReminder.getUsageDosages();
				int a = drugIDs.size();
				int b = usageDosages.size();
				int iBound = a > b ? b : a;
				editor.putInt(head + "drugsNumber", iBound);
				for (int i = 0; i < iBound; i++) {
					editor.putInt(head + "drugID" + i, drugIDs.get(i));
					editor.putString(head + "usageDosage" + i, usageDosages.get(i));
				}
				// save mealIDs
				List<Integer> mealIDs = aReminder.getMealIDs();
				iBound = mealIDs.size();
				editor.putInt(head + "mealsNumber", iBound);
				for (int i = 0; i < iBound; i++)
					editor.putInt(head + "mealID" + i, mealIDs.get(i));
			} else {
				DelayedReminder aReminder = (DelayedReminder) reminder;
				editor.putBoolean(head + "delayed", true);
				editor.putInt(head + "reminderID", aReminder.getReminderID());
				editor.putLong(head + "triggerAtMillis", aReminder.getTriggerAtMillis());
			}
		}
	}
	private static void saveReminders(@NotNull SharedPreferences.Editor editor) {
		editor.putInt("remindersNumber", remindersNumber());
		for (int ID = 0; !reminderIDOutOfBound(ID); ID++)
			saveReminder(ID, editor);
	}
	public static void saveElements(@NotNull Context context) {
		SharedPreferences.Editor editor =
				PreferenceManager.getDefaultSharedPreferences(context).edit();
		saveReminders(editor);
		saveDrugs(editor);
		saveMeals(editor);
		editor.apply();
	}
	private static void loadMeal(int ID, @NotNull SharedPreferences preferences) {
		String head = "meal" + ID;
		if (preferences.getBoolean(head + "exists", false))
			addMeal(new Meal(ID, preferences.getString(head + "name", ""),
					new Time(preferences.getInt(head + "hour", 0),
							preferences.getInt(head + "minute", 0))));
		else deleteMeal(ID);
	}
	private static void loadMeals(@NotNull SharedPreferences preferences) {
		extendMeals(preferences.getInt("mealsNumber", 0) - 1);
		for (int ID = 0; !mealIDOutOfBound(ID); ID++)
			loadMeal(ID, preferences);
	}
	private static void loadDrug(int ID, @NotNull SharedPreferences preferences) {
		String head = "drug" + ID;
		if (preferences.getBoolean(head + "exists", false))
			addDrug(new Drug(ID, preferences.getString(head + "name", ""),
					BitmapCoder.decode(preferences.getString(head + "bitmap", ""))));
		else deleteDrug(ID);
	}
	private static void loadDrugs(@NotNull SharedPreferences preferences) {
		extendDrugs(preferences.getInt("drugsNumber", 0) - 1);
		for (int ID = 0; !drugIDOutOfBound(ID); ID++)
			loadDrug(ID, preferences);
	}
	private static void loadReminder(int ID, @NotNull SharedPreferences preferences) {
		String head = "reminder" + ID;
		if (preferences.getBoolean(head + "exists", false)) {
			IReminder reminder;
			if (preferences.getBoolean(head + "delayed", true)) { // a delayed reminder
				reminder = new DelayedReminder(ID,
						preferences.getInt(head + "reminderID", 0),
						preferences.getLong(head + "triggerAtMillis", System.currentTimeMillis()));
			} else { // an un-delayed reminder
				// load drugIDs and usageDosages
				int drugsNumber = preferences.getInt(head + "drugsNumber", 0);
				List<Integer> drugIDs = new ArrayList<Integer>(drugsNumber);
				List<String> usageDosages = new ArrayList<String>(drugsNumber);
				for (int i = 0; i < drugsNumber; i++) {
					drugIDs.add(preferences.getInt(head + "drugID" + i, i));
					usageDosages.add(preferences.getString(head + "usageDosage" + i, ""));
				}
				// load mealIDs
				int mealsNumber = preferences.getInt(head + "mealsNumber", 0);
				List<Integer> mealIDs = new ArrayList<Integer>(mealsNumber);
				for (int i = 0; i < mealsNumber; i++)
					mealIDs.add(preferences.getInt(head + "mealID" + i, i));
				// create the Reminder instance
				Reminder aReminder = new Reminder(ID, mealIDs,
						preferences.getBoolean(head + "before", true),
						new Time(preferences.getInt(head + "relativeMinutes", 0)),
						drugIDs, usageDosages,
						preferences.getInt(head + "repeatPeriod", 1),
						preferences.getLong(head + "createdTime", System.currentTimeMillis()));
				aReminder.setEnabled(preferences.getBoolean(head + "enabled", true));
				reminder = aReminder;
			}
			addReminder(reminder);
		} else deleteReminder(ID);
	}
	private static void loadReminders(@NotNull SharedPreferences preferences) {
		extendReminders(preferences.getInt("remindersNumber", 0) - 1);
		for (int ID = 0; !reminderIDOutOfBound(ID); ID++)
			loadReminder(ID, preferences);
	}
	public static void loadElements(@NotNull Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		loadReminders(preferences);
		loadDrugs(preferences);
		loadMeals(preferences);
	}
	private static Set<String> codeList(@NotNull List list) {
		Set<String> result = new LinkedHashSet<String>(list.size());
		for (Object object : list) result.add(object.toString());
		return result;
	}
	private static List<Integer> decodeStringSet(@NotNull Set<String> code) {
		List<Integer> result = new ArrayList<Integer>(code.size());
		for (String string : code) result.add(Integer.valueOf(string));
		return result;
	}
}
