package org.ovirt.mobile.movirt.ui.auth.certificatemanagement.data;

import android.content.Context;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.TreeHolder;

public class CertTreeHolder extends TreeHolder<CertTreeItem> {

    public CertTreeHolder(Context context) {
        super(context, R.layout.tree_node_cert, R.layout.tree_image_node_cert, IconDimension.DP_24, true);
    }
}
