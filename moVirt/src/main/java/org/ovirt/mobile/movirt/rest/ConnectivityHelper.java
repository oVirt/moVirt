package org.ovirt.mobile.movirt.rest;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;

@EBean(scope = EBean.Scope.Singleton)
public class ConnectivityHelper {

    @SystemService
    ConnectivityManager connectivityManager;

    public boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
