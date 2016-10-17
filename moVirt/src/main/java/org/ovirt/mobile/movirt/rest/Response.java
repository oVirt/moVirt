package org.ovirt.mobile.movirt.rest;

import android.os.RemoteException;

public interface Response<T> {
    void before();

    void onResponse(T t) throws RemoteException;

    void onError();

    void after();
}
