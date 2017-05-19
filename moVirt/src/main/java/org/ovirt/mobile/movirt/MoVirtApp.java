package org.ovirt.mobile.movirt;

import android.content.Intent;
import android.support.multidex.MultiDexApplication;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;
import org.ovirt.mobile.movirt.provider.UriDependencies;
import org.ovirt.mobile.movirt.ui.mainactivity.MainActivity_;

@EApplication
public class MoVirtApp extends MultiDexApplication {

    // notifies dependent URIs of joins or views
    @Bean
    UriDependencies uriDependencies;

    public void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
