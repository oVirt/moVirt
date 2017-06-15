package org.ovirt.mobile.movirt.ui.mvp;

import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;

public interface AccountPresenter extends BasePresenter {
    AccountPresenter setAccount(MovirtAccount account);

    MovirtAccount getAccount();

    boolean isInstanceOfAccount(MovirtAccount account);
}
