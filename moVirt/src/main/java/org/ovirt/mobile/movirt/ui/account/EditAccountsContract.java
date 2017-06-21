package org.ovirt.mobile.movirt.ui.account;

import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;
import org.ovirt.mobile.movirt.ui.mvp.BaseView;

import java.util.List;

public interface EditAccountsContract {

    interface View extends BaseView {

        void showAccounts(List<AccountWrapper> accounts, ActiveSelection activeSelection);

        void startAddAccountActivity();

        void startSettingsAccountActivity(MovirtAccount account);
    }

    interface Presenter extends BasePresenter {

        void accountClicked(MovirtAccount account);

        void deleteAccount(MovirtAccount account);

        void addAccount();
    }
}

