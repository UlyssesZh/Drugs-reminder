package ulysses.apps.drugsreminder.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class ScrollDisabledListView extends ListView {
    private int mPosition;
    public ScrollDisabledListView(Context context) {
        super(context);
    }
    public ScrollDisabledListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ScrollDisabledListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int actionMasked = ev.getActionMasked() & MotionEvent.ACTION_MASK;
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
            return super.dispatchTouchEvent(ev);
        }
        if (actionMasked == MotionEvent.ACTION_MOVE)
            return true;
        if (actionMasked == MotionEvent.ACTION_UP) {
            if (pointToPosition((int) ev.getX(), (int) ev.getY()) == mPosition) {
                super.dispatchTouchEvent(ev);
            } else {
                setPressed(false);
                invalidate();
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
