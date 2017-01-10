package org.ovirt.mobile.movirt;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;
import org.ovirt.mobile.movirt.provider.UriDependencies;
import org.ovirt.mobile.movirt.ui.MainActivity_;

@EApplication
public class MoVirtApp extends Application {
    private static final String TAG = MoVirtApp.class.getSimpleName();

    // notifies dependent URIs of joins or views
    @Bean
    UriDependencies uriDependencies;

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public void startMainActivity() {
        Intent intent = new Intent(context, MainActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
