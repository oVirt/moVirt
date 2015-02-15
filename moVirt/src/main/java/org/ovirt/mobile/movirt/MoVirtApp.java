package org.ovirt.mobile.movirt;

import android.app.Application;
import android.content.Context;

import org.androidannotations.annotations.EApplication;

@EApplication
public class MoVirtApp extends Application {
    private static final String TAG = MoVirtApp.class.getSimpleName();

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
