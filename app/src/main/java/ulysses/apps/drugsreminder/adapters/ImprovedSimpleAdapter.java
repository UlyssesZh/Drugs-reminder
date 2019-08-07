package ulysses.apps.drugsreminder.adapters;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

public class ImprovedSimpleAdapter extends SimpleAdapter {
    private List<? extends Map<String, ?>> map;
    private String[] from;
    private int layout;
    private int[] to;
    private Context context;
    private LayoutInflater mInflater;
    public ImprovedSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        layout = resource;
        map = data;
        this.from = from;
        this.to = to;
        this.context = context;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return this.createViewFromResource(position, convertView, parent, layout);
    }
    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        View v;
        if (convertView == null)
            v = mInflater.inflate(resource, parent, false);
        else
            v = convertView;
        this.bindView(position, v);
        return v;
    }
    private void bindView(int position, View view) {
        final Map dataSet = map.get(position);
        if (dataSet == null)
            return;
        final ViewBinder binder = super.getViewBinder();
        final int count = to.length;
        for (int i = 0; i < count; i++) {
            final View v = view.findViewById(to[i]);
            if (v != null) {
                final Object data = dataSet.get(from[i]);
                String text = data == null ? "" : data.toString();
                if (text == null)
                    text = "";
                boolean bound = false;
                if (binder != null)
                    bound = binder.setViewValue(v, data, text);
                if (!bound) {
                    if (v instanceof Checkable) {
                        if (data instanceof Boolean) {
                            ((Checkable) v).setChecked((Boolean) data);
                        } else if (data instanceof List && v instanceof CompoundButton) {
                            List list = (List) data;
                            ((Checkable) v).setChecked((Boolean) list.get(0));
                            ((CompoundButton) v).setOnCheckedChangeListener(
                                    (CompoundButton.OnCheckedChangeListener) list.get(1));
                        } else if (v instanceof TextView) {
                            setViewText((TextView) v, text);
                        } else {
                            throw new IllegalStateException(v.getClass().getName() +
                                    " should be bound to a Boolean, not a " +
                                    (data == null ? "<unknown type>" : data.getClass()));
                        }
                    } else if (v instanceof TextView) {
                        setViewText((TextView) v, text);
                    } else if (v instanceof ImageView) {
                        if (data instanceof Integer) {
                            setViewImage((ImageView) v, (Integer) data);
                        } else if (data instanceof Bitmap) {
                            setViewImage((ImageView) v, (Bitmap) data);
                        } else {
                            setViewImage((ImageView) v, text);
                        }
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                " view that can be bounds by this SimpleAdapter");
                    }
                }
            }
        }
    }
    private void setViewImage(@NotNull ImageView v, Bitmap bmp){
        v.setImageBitmap(bmp);
    }
}
