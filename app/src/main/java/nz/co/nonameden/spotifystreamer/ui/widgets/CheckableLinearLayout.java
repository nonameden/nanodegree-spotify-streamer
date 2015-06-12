package nz.co.nonameden.spotifystreamer.ui.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * Created by nonameden on 12/06/15.
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {

    private static final int[]  CHECKED_STATE_SET = { android.R.attr.state_checked };
    private static final int[]  EMPTY_STATE_SET = { };

    private boolean mChecked;

    public CheckableLinearLayout(Context context) {
        super(context);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;

        Drawable background = getBackground();
        if(background!=null) {
            background.setState(mChecked ? CHECKED_STATE_SET : EMPTY_STATE_SET);
            invalidate();
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
}
