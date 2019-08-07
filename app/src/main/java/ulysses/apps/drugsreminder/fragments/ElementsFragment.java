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
import ulysses.apps.drugsreminder.elements.Element;
import ulysses.apps.drugsreminder.adapters.ImprovedSimpleAdapter;

public abstract class ElementsFragment<T extends Element> extends Fragment {
    private List<Integer> IDs;
    private ListView listView;
    private TextView textView;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(layoutFile(), container, false);
        Intent intent = new Intent(getActivity(), editActivityClass());
        textView = root.findViewById(R.id.empty_text);
        listView = root.findViewById(R.id.elements_list);
        refresh();
        listView.setOnItemClickListener(((parent, view, position, id) -> {
            intent.putExtra("ID", IDs.get(position));
            startActivity(intent);
        }));
        root.findViewById(R.id.add_button).setOnClickListener(view -> {
            intent.putExtra("ID", listSize());
            startActivity(intent);
        });
        return root;
    }
    public void refresh() {
        textView.setVisibility(isEmpty() ? View.VISIBLE : View.INVISIBLE);
        IDs = new ArrayList<Integer>();
        List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
        for (int ID = 0; ID < listSize(); ID++) {
            if (doesNotHave(ID)) continue;
            int stringsNumber = fromStrings().length;
            Map<String, Object> listItem = new HashMap<String, Object>(stringsNumber);
            for(int i = 0; i < stringsNumber; i++)
                listItem.put(fromStrings()[i], findContentFromStringIndex(getElement(ID), i));
            listItems.add(listItem);
            IDs.add(ID);
        }
        listView.setAdapter(new ImprovedSimpleAdapter(getActivity(), listItems, dataLayoutFile(),
                fromStrings(), toStrings()));
    }
    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
    protected abstract int listSize();
    protected abstract boolean isEmpty();
    protected abstract int layoutFile();
    protected abstract int dataLayoutFile();
    protected abstract String[] fromStrings();
    protected abstract int[] toStrings();
    protected abstract Object findContentFromStringIndex(T element, int stringIndex);
    protected abstract boolean doesNotHave(int ID);
    protected abstract Class editActivityClass();
    protected abstract T getElement(int ID);
}
