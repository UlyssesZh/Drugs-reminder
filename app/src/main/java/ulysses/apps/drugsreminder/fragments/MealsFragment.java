package ulysses.apps.drugsreminder.fragments;

import ulysses.apps.drugsreminder.activities.EditMealActivity;
import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;
import ulysses.apps.drugsreminder.elements.Meal;

public class MealsFragment extends ElementsFragment<Meal> {
    @Override
    protected int listSize() {
        return ElementsLibrary.mealsNumber();
    }
    @Override
    protected int layoutFile() {
        return R.layout.meals_fragment;
    }
    @Override
    protected int dataLayoutFile() {
        return R.layout.meal_item;
    }
    @Override
    protected String[] fromStrings() {
        return new String[]{"name", "time"};
    }
    @Override
    protected int[] toStrings() {
        return new int[]{R.id.meal_name, R.id.meal_time};
    }
    @Override
    protected Object findContentFromStringIndex(Meal meal, int stringIndex) {
        switch (stringIndex) {
            case 0:
                return meal.getName();
            case 1:
                return meal.getTime().toString();
        }
        return null;
    }
    @Override
    protected boolean doesNotHave(int ID) {
        return ElementsLibrary.doesNotHaveMeal(ID);
    }
    @Override
    protected Class editActivityClass() {
        return EditMealActivity.class;
    }
    @Override
    protected boolean isEmpty() {
        return ElementsLibrary.doesNotHaveMeals();
    }
    @Override
    protected Meal getElement(int ID) {
        return ElementsLibrary.findMealByID(ID);
    }
}
