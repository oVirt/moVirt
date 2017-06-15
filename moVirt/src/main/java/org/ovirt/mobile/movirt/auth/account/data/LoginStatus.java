package org.ovirt.mobile.movirt.auth.account.data;

public class LoginStatus extends Status {

    public LoginStatus(MovirtAccount account, boolean loginInProgress) {
        super(account, loginInProgress);
    }
}
