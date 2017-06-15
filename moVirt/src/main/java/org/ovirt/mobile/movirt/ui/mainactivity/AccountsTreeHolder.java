package org.ovirt.mobile.movirt.ui.mainactivity;

import android.content.Context;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.IconDimension;
import org.ovirt.mobile.movirt.ui.TreeHolder;

public class AccountsTreeHolder extends TreeHolder<AccountsTreeItem> {

    public AccountsTreeHolder(Context context) {
        super(context, R.layout.tree_node_accounts, R.layout.tree_image_node_accounts, IconDimension.DP_36, false);
    }
}
