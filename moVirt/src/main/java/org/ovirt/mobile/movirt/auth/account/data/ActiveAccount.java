package org.ovirt.mobile.movirt.auth.account.data;

import android.accounts.Account;

public class ActiveAccount {
    public static final ActiveAccount ALL_ACTIVE = new ActiveAccount();

    private Account account;

    private ActiveAccount() {
    }

    public ActiveAccount(Account account) {
        this.account = account;
    }

    public boolean isAccount(Account account) {
        return this.account != null ? this.account.equals(account) : account == null;
    }

    public boolean isAllAccounts() {
        return account == null;
    }

    public Account getAccount() {
        return account;
    }

    public String getAccountName() {
        return account == null ? "" : account.name;
    }
}
