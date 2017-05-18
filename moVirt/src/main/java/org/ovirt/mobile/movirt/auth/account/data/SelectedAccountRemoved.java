package org.ovirt.mobile.movirt.auth.account.data;

public class SelectedAccountRemoved {

    private final MovirtAccount account;

    private final ActiveSelection activeSelection;

    public SelectedAccountRemoved(MovirtAccount account, ActiveSelection activeSelection) {
        this.account = account;
        this.activeSelection = activeSelection;
    }

    public boolean isRemoved() {
        return activeSelection.isAccount(account);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectedAccountRemoved)) return false;

        SelectedAccountRemoved that = (SelectedAccountRemoved) o;

        if (account != null ? !account.equals(that.account) : that.account != null) return false;
        return activeSelection != null ? activeSelection.equals(that.activeSelection) : that.activeSelection == null;
    }

    @Override
    public int hashCode() {
        int result = account != null ? account.hashCode() : 0;
        result = 31 * result + (activeSelection != null ? activeSelection.hashCode() : 0);
        return result;
    }
}
