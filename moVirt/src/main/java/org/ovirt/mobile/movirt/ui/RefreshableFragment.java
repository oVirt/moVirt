package org.ovirt.mobile.movirt.ui;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

@EFragment
public abstract class RefreshableFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, HasProgressBar {

    protected abstract SwipeRefreshLayout getSwipeRefreshLayout();

    @AfterViews
    protected void initRefreshLayout() {
        getSwipeRefreshLayout().setOnRefreshListener(this);
    }

    @Override
    @UiThread
    public void showProgressBar() {
        getSwipeRefreshLayout().setRefreshing(true);
    }

    @Override
    @UiThread
    public void hideProgressBar() {
        getSwipeRefreshLayout().setRefreshing(false);
    }

    protected void setRefreshing(boolean refreshing) {
        if (refreshing) {
            showProgressBar();
        } else {
            hideProgressBar();
        }
    }
}
