package org.ovirt.mobile.movirt.ui;

import org.ovirt.mobile.movirt.sync.SimpleResponse;

/**
 * Base class for OVirtClient {@link org.ovirt.mobile.movirt.sync.Response}s that
 * want to show and hide progressbar during their execution.
 */
public class ProgressBarResponse<T> extends SimpleResponse<T> {

    private final HasProgressBar hasProgressBar;

    public ProgressBarResponse(HasProgressBar hasProgressBar) {
        this.hasProgressBar = hasProgressBar;
    }

    @Override
    public void before() {
        hasProgressBar.showProgressBar();
    }

    @Override
    public void after() {
        hasProgressBar.hideProgressBar();
    }
}
