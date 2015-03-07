package org.ovirt.mobile.movirt.ui;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.View;

public class TabChangedListener implements ActionBar.TabListener  {
    private View view;
    private CurrentlyShown shown;
    private HasCurrentlyShown hasCurrentlyShown;

    TabChangedListener(View view, CurrentlyShown shown, HasCurrentlyShown hasCurrentlyShown) {
        this.view = view;
        this.shown = shown;
        this.hasCurrentlyShown = hasCurrentlyShown;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        view.setVisibility(View.VISIBLE);
        hasCurrentlyShown.setCurrentlyShown(shown);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        view.setVisibility(View.GONE);
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    public static enum CurrentlyShown {
        VMS, EVENTS,
        VM_DETAIL_GENERAL, DISKS
    }

    public static interface HasCurrentlyShown {
        void setCurrentlyShown(CurrentlyShown currentlyShown);
    }
}
