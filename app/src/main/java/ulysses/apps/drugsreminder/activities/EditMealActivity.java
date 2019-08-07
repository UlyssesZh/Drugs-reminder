package ulysses.apps.drugsreminder.activities;

import android.widget.EditText;
import android.widget.TimePicker;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.elements.Meal;
import ulysses.apps.drugsreminder.util.Time;

public class EditMealActivity extends EditElementActivity<Meal> {
    private EditText editName;
    private TimePicker editTime;
    @Override
    protected int layoutFile() {
        return R.layout.edit_meal_activity;
    }
    @Override
    protected void setupViews() {
        editName = findViewById(R.id.edit_meal_name);
        editTime = findViewById(R.id.edit_meal_time);
    }
    @Override
    protected void loadViews(Meal meal) {
        editName.setText(meal.getName());
        editTime.setHour(meal.getTime().getHour());
        editTime.setMinute(meal.getTime().getMinute());
    }
    @Override
    protected void deleteElement(int ID) {
        ElementsLibrary.deleteMeal(ID);
    }
    @Override
    protected boolean saveChanges(int ID) {
        String mealName = editName.getText().toString();
        if (mealName.isEmpty()) {
            alert(R.string.name_empty_hint);
            return false;
        } else {
            ElementsLibrary.addMeal(new Meal(ID, mealName, new Time(
                    editTime.getHour(), editTime.getMinute())));
            return true;
        }
    }
    @Override
    protected boolean isNotCreating(int ID) {
        return !ElementsLibrary.mealIDOutOfBound(ID);
    }
    @Override
    protected Meal getElement(int ID) {
        return ElementsLibrary.findMealByID(ID);
    }
}
