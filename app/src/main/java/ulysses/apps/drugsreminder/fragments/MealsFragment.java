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
    protected int layoutId() {
        return R.layout.meals_fragment;
    }
    @Override
    protected int listItemLayoutId() {
        return R.layout.meal_item;
    }
    @Override
    protected String[] from() {
        return new String[]{"name", "time"};
    }
    @Override
    protected int[] to() {
        return new int[]{R.id.meal_name, R.id.meal_time};
    }
    @Override
    protected Object findContentFromIndex(Meal meal, int index) {
        switch (index) {
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
    protected Class<EditMealActivity> editActivityClass() {
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
