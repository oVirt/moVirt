package org.ovirt.mobile.movirt.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

@EService
public class AuthenticatorService extends Service {

    private static final String TAG = AuthenticatorService.class.getSimpleName();

    @Bean
    MovirtAuthenticator authenticator;

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
