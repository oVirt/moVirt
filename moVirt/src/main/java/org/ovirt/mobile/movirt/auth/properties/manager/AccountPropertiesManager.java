package org.ovirt.mobile.movirt.auth.properties.manager;

import android.accounts.AccountManagerFuture;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.properties.AccountPropertiesRW;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.auth.properties.property.CertLocation;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
import org.ovirt.mobile.movirt.rest.dto.Api;

public class AccountPropertiesManager extends AccountPropertiesManagerCore {
    private static final String TAG = AccountPropertiesManager.class.getSimpleName();

    public AccountPropertiesManager(AccountPropertiesRW accountPropertiesRW) {
        super(accountPropertiesRW);
    }

    @SuppressWarnings("unchecked")
    public AccountManagerFuture<Bundle> getAuthToken() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.FUTURE_AUTH_TOKEN, AccountManagerFuture.class);
    }

    public String peekAuthToken() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.PEEK_AUTH_TOKEN, String.class);
    }

    public boolean setAuthToken(String token) throws AccountDeletedException {
        return setAuthToken(token, OnThread.CURRENT);
    }

    public boolean setAuthToken(String token, OnThread runOnThread) throws AccountDeletedException {
        return setAndNotify(AccountProperty.AUTH_TOKEN, token, runOnThread);
    }

    public Boolean isFirstLogin() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.FIRST_LOGIN, Boolean.class);
    }

    public boolean setFirstLogin(Boolean firstLogin) throws AccountDeletedException {
        return setFirstLogin(firstLogin, OnThread.CURRENT);
    }

    public boolean setFirstLogin(Boolean firstLogin, OnThread runOnThread) throws AccountDeletedException {
        return setAndNotify(AccountProperty.FIRST_LOGIN, firstLogin, runOnThread);
    }

    public String getUsername() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.USERNAME, String.class);
    }

    public boolean setUsername(String username) throws AccountDeletedException {
        return setUsername(username, OnThread.CURRENT);
    }

    public boolean setUsername(String username, OnThread runOnThread) throws AccountDeletedException {
        return setAndNotify(AccountProperty.USERNAME, username, runOnThread);
    }

    public String getPassword() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.PASSWORD, String.class);
    }

    public boolean setPassword(String password) throws AccountDeletedException {  // triggers sync in later APIs (Android 6)
        return setPassword(password, OnThread.CURRENT);
    }

    public boolean setPassword(String password, OnThread runOnThread) throws AccountDeletedException {  // triggers sync in later APIs (Android 6)
        return setAndNotify(AccountProperty.PASSWORD, password, runOnThread);
    }

    public String getApiUrl() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.API_URL, String.class);
    }

    public boolean setApiUrl(String apiUrl) throws AccountDeletedException {
        return setApiUrl(apiUrl, OnThread.CURRENT);
    }

    public boolean setApiUrl(String apiUrl, OnThread runOnThread) throws AccountDeletedException {
        return setAndNotify(AccountProperty.API_URL, apiUrl, runOnThread);
    }

    public String getApiBaseUrl() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.API_BASE_URL, String.class);
    }

    @NonNull
    public Version getApiVersion() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.VERSION, Version.class);
    }

    public boolean setApiVersion(Api newApi) throws AccountDeletedException {
        return setApiVersion(newApi, OnThread.CURRENT);
    }

    public boolean setApiVersion(Api newApi, OnThread runOnThread) throws AccountDeletedException {
        Version newVersion = newApi == null ? null : newApi.toVersion();
        return setAndNotify(AccountProperty.VERSION, newVersion, runOnThread);
    }

    @NonNull
    public CertHandlingStrategy getCertHandlingStrategy() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.CERT_HANDLING_STRATEGY, CertHandlingStrategy.class);
    }

    public boolean setCertHandlingStrategy(CertHandlingStrategy certHandlingStrategy) throws AccountDeletedException {
        return setCertHandlingStrategy(certHandlingStrategy, OnThread.CURRENT);
    }

    public boolean setCertHandlingStrategy(CertHandlingStrategy certHandlingStrategy, OnThread runOnThread) throws AccountDeletedException {
        return setAndNotify(AccountProperty.CERT_HANDLING_STRATEGY, certHandlingStrategy, runOnThread);
    }

    public Boolean hasAdminPermissions() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.HAS_ADMIN_PERMISSIONS, Boolean.class);
    }

    public boolean setAdminPermissions(Boolean hasAdminPermissions) throws AccountDeletedException {
        return setAdminPermissions(hasAdminPermissions, OnThread.CURRENT);
    }

    public boolean setAdminPermissions(Boolean hasAdminPermissions, OnThread runOnThread) throws AccountDeletedException {
        return setAndNotify(AccountProperty.HAS_ADMIN_PERMISSIONS, hasAdminPermissions, runOnThread);
    }

    @NonNull
    public Cert[] getCertificateChain() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.CERTIFICATE_CHAIN, Cert[].class);
    }

    public boolean setCertificateChain(Cert[] certChain) throws AccountDeletedException {
        return setCertificateChain(certChain, OnThread.CURRENT);
    }

    public boolean setCertificateChain(Cert[] certChain, OnThread runOnThread) throws AccountDeletedException {
        return setAndNotify(AccountProperty.CERTIFICATE_CHAIN, certChain, runOnThread);
    }

    @NonNull
    public String getValidHostnames() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.VALID_HOSTNAMES, String.class);
    }

    @NonNull
    public String[] getValidHostnameList() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.VALID_HOSTNAME_LIST, String[].class);
    }

    public boolean setValidHostnameList(String[] hostnameList) throws AccountDeletedException {
        return setValidHostnameList(hostnameList, OnThread.CURRENT);
    }

    public boolean setValidHostnameList(String[] hostnameList, OnThread runOnThread) throws AccountDeletedException {
        return setAndNotify(AccountProperty.VALID_HOSTNAME_LIST, hostnameList, runOnThread);
    }

    @NonNull
    public CertLocation getCertificateLocation() throws AccountDeletedException {
        return accountPropertiesRW.getResource(AccountProperty.CERTIFICATE_LOCATION, CertLocation.class);
    }

    public boolean setCertificateLocation(CertLocation certificateLocation) throws AccountDeletedException {
        return setCertificateLocation(certificateLocation, OnThread.CURRENT);
    }

    public boolean setCertificateLocation(CertLocation certificateLocation, OnThread runOnThread) throws AccountDeletedException {
        return setAndNotify(AccountProperty.CERTIFICATE_LOCATION, certificateLocation, runOnThread);
    }
}
