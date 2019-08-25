package ulysses.apps.drugsreminder.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.activities.EditElementActivity;
import ulysses.apps.drugsreminder.elements.IElement;
import ulysses.apps.drugsreminder.adapters.ImprovedSimpleAdapter;

public abstract class ElementsFragment<T extends IElement> extends Fragment {
	private List<Integer> IDs;
	private ListView elementsList;
	private TextView emptyText;
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View root = inflater.inflate(layoutId(), container, false);
		emptyText = root.findViewById(R.id.empty_text);
		elementsList = root.findViewById(R.id.elements_list);
		refresh();
		elementsList.setOnItemClickListener((parent, view, position, id) -> {
			int ID = IDs.get(position);
			if (shouldEdit(getElement(ID))) startEditActivity(ID);
		});
		root.findViewById(R.id.add_button).setOnClickListener(
				view -> startEditActivity(listSize()));
		return root;
	}
	public void refresh() {
		emptyText.setVisibility(isEmpty() ? View.VISIBLE : View.INVISIBLE);
		IDs = new ArrayList<>();
		List<Map<String, Object>> listItems = new ArrayList<>();
		for (int ID = 0; ID < listSize(); ID++) {
			if (doesNotHave(ID)) continue;
			int stringsNumber = from().length;
			Map<String, Object> listItem = new HashMap<String, Object>(stringsNumber);
			for(int i = 0; i < stringsNumber; i++)
				listItem.put(from()[i], findContentFromIndex(getElement(ID), i));
			listItems.add(listItem);
			IDs.add(ID);
		}
		elementsList.setAdapter(new ImprovedSimpleAdapter(getActivity(), listItems,
				listItemLayoutId(), from(), to()));
	}
	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}
	private void startEditActivity(int ID) {
		Intent intent = new Intent(getActivity(), editActivityClass());
		intent.putExtra("ID", ID);
		startActivity(intent);
	}
	protected boolean shouldEdit(T element) {
		return true;
	}
	/** @return how many elements there totally are.*/
	protected abstract int listSize();
	/** @return whether there are no elements in the list. Used to decide whether to show
	 * {@link #emptyText}.*/
	protected abstract boolean isEmpty();
	/** @return the layout file resource id. Used to inflate the fragment's root.*/
	protected abstract int layoutId();
	/** @return the layout file resource id for items in {@link #elementsList}.*/
	protected abstract int listItemLayoutId();
	/** @return an arbitrary list of String.*/
	protected abstract String[] from();
	/** @return a list of views' id which has been defined in the file designated in
	 * {@link #listItemLayoutId()}. The length of it should be the same as that of
	 * #{@link #from()}.*/
	protected abstract int[] to();
	/** @return a list of objects that describes the contents of the views designated in
	 * {@link #to()}.*/
	protected abstract Object findContentFromIndex(T element, int index);
	/** @return whether there is not an element of the provided ID.*/
	protected abstract boolean doesNotHave(int ID);
	/** @return the class of the activity that will be launched when the user tries to edit / create
	 * an element.*/
	protected abstract Class<? extends EditElementActivity<T>> editActivityClass();
	/** @return get the element of the provided ID.*/
	protected abstract T getElement(int ID);
}
