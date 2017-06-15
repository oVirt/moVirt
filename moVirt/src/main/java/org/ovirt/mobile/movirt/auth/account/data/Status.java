package org.ovirt.mobile.movirt.auth.account.data;

abstract class Status {
    private final MovirtAccount account;
    private final boolean inProgress;

    public Status(MovirtAccount account, boolean inProgress) {
        this.account = account;
        this.inProgress = inProgress;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public boolean isAccount(MovirtAccount account) {
        return this.account != null ? this.account.equals(account) : account == null;
    }

    public MovirtAccount getAccount() {
        return account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Status)) return false;

        Status that = (Status) o;

        if (inProgress != that.inProgress) return false;
        return account != null ? account.equals(that.account) : that.account == null;
    }

    @Override
    public int hashCode() {
        int result = account != null ? account.hashCode() : 0;
        result = 31 * result + (inProgress ? 1 : 0);
        return result;
    }
}
