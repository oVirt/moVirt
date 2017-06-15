package org.ovirt.mobile.movirt.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

@EService
public class AuthenticatorService extends Service {

    @Bean
    MovirtAuthenticator authenticator;

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
