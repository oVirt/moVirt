package org.ovirt.mobile.movirt.rest;

import android.os.RemoteException;

public abstract class SimpleResponse<T> implements Response<T> {

    @Override
    public void before() {
        // do nothing
    }

    @Override
    public void onResponse(T t) throws RemoteException {
        // do nothing
    }

    @Override
    public void onError() {
        // do nothing
    }

    @Override
    public void after() {
        // do nothing
    }
}
