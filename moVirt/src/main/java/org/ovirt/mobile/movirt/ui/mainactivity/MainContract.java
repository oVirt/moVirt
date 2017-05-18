package org.ovirt.mobile.movirt.ui.mainactivity;

import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;
import org.ovirt.mobile.movirt.ui.mvp.BaseView;
import org.ovirt.mobile.movirt.ui.mvp.StatusView;

import java.util.List;
import java.util.SortedMap;

public interface MainContract {

    interface View extends BaseView, StatusView {

        void showAccountDialog();

        void showAccountsAndClusters(SortedMap<MovirtAccount, List<Cluster>> accountsAndClusters);

        void selectActiveSelection(ActiveSelection activeSelection);

        void startEditAccountsActivity();

        void startAccountSettingsActivity(MovirtAccount account);

        void hideDrawer();
    }

    interface Presenter extends BasePresenter {

        void onActiveSelectionChanged(ActiveSelection activeSelection);

        void onLongClickListener(ActiveSelection possibleSelection);
    }
}

