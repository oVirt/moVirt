package org.ovirt.mobile.movirt.provider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {
    private OVirtAccountAuthenticator authenticator;

    @Override
    public void onCreate() {
        super.onCreate();

        authenticator = new OVirtAccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
