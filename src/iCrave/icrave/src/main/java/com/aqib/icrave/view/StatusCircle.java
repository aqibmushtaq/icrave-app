package com.aqib.icrave.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.aqib.icrave.R;

/**
 * Created by Aqib on 26/01/14.
 */
public class StatusCircle extends ImageView {

    private static final int[] STATE = {R.attr.state_synced};

    private boolean isSynced = false;

    public StatusCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if (isSynced)
            mergeDrawableStates(drawableState, STATE);

        return drawableState;
    }

    /**
     * Set the sync state of the icon
     * @param isSynced Sync status, true for synced and false otherwise
     */
    public void setSynced (boolean isSynced) {
        if (this.isSynced != isSynced) {
            this.isSynced = isSynced;
            refreshDrawableState();
        }
    }

}
