package ulysses.apps.drugsreminder.elements;

import java.util.ArrayList;
import java.util.List;

import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.util.Time;

public class Meal implements IElement {
	private int ID;
	private Time time;
	private String name;
	public Meal(int ID, String name, Time time) {
		this.ID = ID;
		this.name = name;
		this.time = time;
	}
	@Override
	public int getID() {
		return ID;
	}
	public Time getTime() {
		return time;
	}
	public String getName() {
		return name;
	}
	@Override
	public List<Integer> getInvolvingReminderIDs() {
		List<Integer> result = new ArrayList<>();
		for (int reminderID = 0; reminderID < ElementsLibrary.remindersNumber(); reminderID++)
			if (!ElementsLibrary.doesNotHaveReminder(reminderID)) {
				IReminder reminder = ElementsLibrary.findReminderByID(reminderID);
				if (reminder.isRepeating() && ((Reminder) reminder).getMealIDs().contains(ID))
					result.add(reminderID);
			}
		return result;
	}
}
