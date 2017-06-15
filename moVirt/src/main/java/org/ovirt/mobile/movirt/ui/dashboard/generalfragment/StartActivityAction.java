package org.ovirt.mobile.movirt.ui.dashboard.generalfragment;

import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.ui.MainActivityFragments;

public class StartActivityAction {
    private MainActivityFragments fragment;
    private String orderBy;
    private SortOrder order;

    public StartActivityAction(MainActivityFragments fragment, String orderBy, SortOrder order) {
        this.fragment = fragment;
        this.orderBy = orderBy;
        this.order = order;
    }

    public MainActivityFragments getFragment() {
        return fragment;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public SortOrder getOrder() {
        return order;
    }
}
