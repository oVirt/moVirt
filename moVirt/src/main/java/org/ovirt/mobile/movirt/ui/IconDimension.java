package org.ovirt.mobile.movirt.ui;

import android.support.annotation.DrawableRes;

import org.ovirt.mobile.movirt.R;

public enum IconDimension {
    DP_24(R.drawable.ic_chevron_right_white_24dp, R.drawable.ic_expand_more_white_24dp),
    DP_36(R.drawable.ic_chevron_right_white_36dp, R.drawable.ic_expand_more_white_36dp);

    @DrawableRes
    private final int rightIconId;
    @DrawableRes
    private final int moreIconId;

    IconDimension(@DrawableRes int rightIconId, @DrawableRes int moreIconId) {
        this.rightIconId = rightIconId;
        this.moreIconId = moreIconId;
    }

    @DrawableRes
    public int getResource(boolean activated) {
        return activated ? moreIconId : rightIconId;
    }
}
