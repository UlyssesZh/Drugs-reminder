package ulysses.apps.drugsreminder.elements;

import java.util.List;

public interface Element {
    int getID();
    List<Integer> getInvolvingReminderIDs();
}
