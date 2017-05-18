package org.ovirt.mobile.movirt.auth.account.data;

public class SyncStatus extends Status {

    public SyncStatus(boolean syncInProgress) {
        super(null, syncInProgress);
    }

    public SyncStatus(MovirtAccount account, boolean syncInProgress) {
        super(account, syncInProgress);
    }
}
