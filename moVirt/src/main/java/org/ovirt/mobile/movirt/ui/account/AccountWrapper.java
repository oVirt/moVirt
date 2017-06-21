package org.ovirt.mobile.movirt.ui.account;

import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;

class AccountWrapper {
    MovirtAccount account;
    Version version;

    public AccountWrapper(MovirtAccount account, Version version) {
        this.account = account;
        this.version = version;
    }
}
