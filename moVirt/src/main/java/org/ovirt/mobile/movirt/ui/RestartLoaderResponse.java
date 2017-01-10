package org.ovirt.mobile.movirt.ui;

import org.ovirt.mobile.movirt.rest.SimpleResponse;

public class RestartLoaderResponse<T> extends SimpleResponse<T> {

    private final HasLoader hasLoader;

    public RestartLoaderResponse(HasLoader hasLoader) {
        this.hasLoader = hasLoader;
    }

    @Override
    public void after() {
        hasLoader.restartLoader();
    }
}
