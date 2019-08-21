package ulysses.apps.drugsreminder.elements;

import java.util.List;

public interface IElement {
    int getID();
    List<Integer> getInvolvingReminderIDs();
}
