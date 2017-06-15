package org.ovirt.mobile.movirt.ui.auth.connectionsettings.exception;

import android.support.annotation.NonNull;

import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.util.ObjectUtils;

public class WrongArgumentException extends Exception {

    private final MovirtAccount account;

    public WrongArgumentException(MovirtAccount account, String message) {
        super(message);
        ObjectUtils.requireNotNull(account, "account");
        this.account = account;
    }

    @NonNull
    public MovirtAccount getAccount() {
        return account;
    }
}
