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
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

public class ImprovedSimpleAdapter extends SimpleAdapter {
	private List<? extends Map<String, ?>> dataList;
	private String[] from;
	private int layoutId;
	private int[] to;
	private Context context;
	private LayoutInflater mInflater;
	public ImprovedSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource,
	                             String[] from, int[] to) {
		super(context, data, resource, from, to);
		layoutId = resource;
		dataList = data;
		this.from = from;
		this.to = to;
		this.context = context;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return this.createViewFromResource(position, convertView, parent, layoutId);
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
		Map dataSet = dataList.get(position);
		if (dataSet == null) return;
		ViewBinder binder = super.getViewBinder();
		int count = to.length;
		for (int i = 0; i < count; i++) {
			View v = view.findViewById(to[i]);
			if (v != null) {
				Object data = dataSet.get(from[i]);
				String text = data == null ? "" : data.toString();
				if (text == null) text = "";
				boolean bound = false;
				if (binder != null) bound = binder.setViewValue(v, data, text);
				if (!bound) {
					if (v instanceof Checkable) {
						if (data instanceof Boolean) {
							((Checkable) v).setChecked((boolean) data);
						} else if (data instanceof Object[] && v instanceof CompoundButton) {
							setButtonStateAndListener((CompoundButton) v, (Object[]) data);
						} else if (v instanceof TextView) {
							setViewText((TextView) v, text);
						} else {
							throw new IllegalStateException(
									v.getClass().getName() +
											" could not be bound to a " +
											(data == null ? "<unknown type>" : data.getClass()));
						}
					} else if (v instanceof TextView) {
						setViewText((TextView) v, text);
					} else if (v instanceof ImageView) {
						if (data instanceof Integer) {
							setViewImage((ImageView) v, (int) data);
						} else if (data instanceof Bitmap) {
							setViewImage((ImageView) v, (Bitmap) data);
						} else {
							setViewImage((ImageView) v, text);
						}
					} else {
						throw new IllegalStateException(
								v.getClass().getName() +
										" is not a view that can be bound by this adapter");
					}
				}
			}
		}
	}
	private void setViewImage(@NotNull ImageView v, Bitmap bmp){
		v.setImageBitmap(bmp);
	}
	private void setButtonStateAndListener(@NotNull CompoundButton v, @NotNull Object[] data) {
		if (data[1] instanceof CompoundButton.OnCheckedChangeListener)
			v.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) data[1]);
		if (data[0] instanceof Boolean) v.setChecked((boolean) data[0]);
	}
}
