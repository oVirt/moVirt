package org.ovirt.mobile.movirt.ui.auth.connectionsettings.exception;

import android.support.annotation.NonNull;

import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.LoginInfo;
import org.ovirt.mobile.movirt.util.ObjectUtils;

public class WrongApiPathException extends Exception {
    private final LoginInfo loginInfo;
    private final MovirtAccount account;

    public WrongApiPathException(MovirtAccount account, LoginInfo loginInfo) {
        super();
        ObjectUtils.requireNotNull(account, "account");
        ObjectUtils.requireNotNull(loginInfo, "loginInfo");
        this.account = account;
        this.loginInfo = loginInfo;
    }

    @NonNull
    public MovirtAccount getAccount() {
        return account;
    }

    @NonNull
    public LoginInfo getLoginInfo() {
        return loginInfo;
    }
}
