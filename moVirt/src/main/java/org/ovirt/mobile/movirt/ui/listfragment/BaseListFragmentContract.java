package org.ovirt.mobile.movirt.ui.listfragment;

import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortEntry;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;
import org.ovirt.mobile.movirt.ui.mvp.BaseView;
import org.ovirt.mobile.movirt.util.Disposables;

public interface BaseListFragmentContract {

    interface View extends BaseView {
        void displaySelection(SortEntry sortEntry, SortOrder sortOrder);

        void refreshDisplay();
    }

    interface Presenter extends BasePresenter {

        Disposables getDisposables();

        Presenter initAllSortEntries(SortEntry[] sortEntries);

        Presenter initSelection(SortEntry sortEntry, SortOrder sortOrder);

        void setSelection(String sortBy, SortOrder sortOrder);

        void onOrderBySelected(SortEntry sortEntry);

        void onOrderSelected(SortOrder sortOrder);
    }
}

