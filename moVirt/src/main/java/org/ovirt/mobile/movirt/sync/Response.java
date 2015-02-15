package org.ovirt.mobile.movirt.sync;

import android.os.RemoteException;

public interface Response<T> {
    void before();

    void onResponse(T t) throws RemoteException;

    void onError();

    void after();
}
