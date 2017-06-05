package org.ovirt.mobile.movirt.ui.mvp;

import org.ovirt.mobile.movirt.auth.account.data.Selection;

public interface StatusView {

    void displayStatus(String status);

    void displayStatus(Selection selection);

    void displayTitle(String title);
}
