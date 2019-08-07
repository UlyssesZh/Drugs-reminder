package ulysses.apps.drugsreminder.adapters;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.fragments.DrugsFragment;
import ulysses.apps.drugsreminder.fragments.MealsFragment;
import ulysses.apps.drugsreminder.fragments.RemindersFragment;
import ulysses.apps.drugsreminder.fragments.SettingsFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.reminders_tab, R.string.drugs_tab, R.string.meals_tab, R.string.settings_tab};
    private final Context context;
    public SectionsPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;
    }
    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return new RemindersFragment();
            case 1:
                return new DrugsFragment();
            case 2:
                return new MealsFragment();
            case 3:
                return new SettingsFragment();
        }
        return null;
    }
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return context.getResources().getString(TAB_TITLES[position]);
    }
    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}