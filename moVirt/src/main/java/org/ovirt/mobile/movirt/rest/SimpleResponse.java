package org.ovirt.mobile.movirt.rest;

import android.os.RemoteException;

public abstract class SimpleResponse<T> implements Response<T> {
    private static final SimpleResponse DUMMY_RESPONSE = new DummyResponse();

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

    @SuppressWarnings("unchecked")
    public static <T> SimpleResponse<T> dummyResponse() {
        return (SimpleResponse<T>) DUMMY_RESPONSE;
    }

    private static final class DummyResponse<T> extends SimpleResponse<T> {
    }
}
