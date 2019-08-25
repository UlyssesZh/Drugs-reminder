package ulysses.apps.drugsreminder.fragments;

import ulysses.apps.drugsreminder.activities.EditDrugActivity;
import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.elements.Drug;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;

public class DrugsFragment extends ElementsFragment<Drug> {
	@Override
	protected int listSize() {
		return ElementsLibrary.drugsNumber();
	}
	@Override
	protected int layoutId() {
		return R.layout.drugs_fragment;
	}
	@Override
	protected int listItemLayoutId() {
		return R.layout.drug_item;
	}
	@Override
	protected String[] from() {
		return new String[] {"name", "image"};
	}
	@Override
	protected int[] to() {
		return new int[] {R.id.drug_name, R.id.drug_image};
	}
	@Override
	protected Object findContentFromIndex(Drug drug, int index) {
		switch (index) {
			case 0:
				return drug.getName();
			case 1:
				return drug.getBitmap();
		}
		return null;
	}
	@Override
	protected boolean doesNotHave(int ID) {
		return ElementsLibrary.doesNotHaveDrug(ID);
	}
	@Override
	protected Class<EditDrugActivity> editActivityClass() {
		return EditDrugActivity.class;
	}
	@Override
	protected boolean isEmpty() {
		return ElementsLibrary.doesNotHaveDrugs();
	}
	@Override
	protected Drug getElement(int ID) {
		return ElementsLibrary.findDrugByID(ID);
	}
}
