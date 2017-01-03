package org.ovirt.mobile.movirt.auth.properties.manager;

import android.accounts.AccountManagerFuture;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.auth.properties.property.Version;
import org.ovirt.mobile.movirt.rest.dto.Api;

import java.util.Set;

/**
 * {@inheritDoc}
 */
@EBean(scope = EBean.Scope.Singleton)
public class AccountPropertiesManager extends AccountPropertiesManagerCore {
    private static final String TAG = AccountPropertiesManager.class.getSimpleName();

    @Bean
    @Override
    void setAuthenticator(MovirtAuthenticator authenticator) {
        super.setAuthenticator(authenticator);
    }

    @Background
    @Override
    void notifyBackgroundListeners(Set<WrappedPropertyChangedListener> backgroundListeners, Object o) {
        notifyListeners(backgroundListeners, o);
    }

    @SuppressWarnings("unchecked")
    public AccountManagerFuture<Bundle> getAuthToken() {
        return authenticator.getResource(AccountProperty.FUTURE_AUTH_TOKEN, AccountManagerFuture.class);
    }

    public String peekAuthToken() {
        return authenticator.getResource(AccountProperty.PEEK_AUTH_TOKEN, String.class);
    }

    public boolean setAuthToken(String token) {
        return setAuthToken(token, OnThread.CURRENT);
    }

    public boolean setAuthToken(String token, OnThread runOnThread) {
        return setAndNotify(AccountProperty.AUTH_TOKEN, token, runOnThread);
    }

    public Boolean accountConfigured() {
        return authenticator.getResource(AccountProperty.ACCOUNT_CONFIGURED, Boolean.class);
    }

    public Boolean isFirstLogin() {
        return authenticator.getResource(AccountProperty.FIRST_LOGIN, Boolean.class);
    }

    public boolean setFirstLogin(Boolean firstLogin) {
        return setFirstLogin(firstLogin, OnThread.CURRENT);
    }

    public boolean setFirstLogin(Boolean firstLogin, OnThread runOnThread) {
        return setAndNotify(AccountProperty.FIRST_LOGIN, firstLogin, runOnThread);
    }

    public String getUsername() {
        return authenticator.getResource(AccountProperty.USERNAME, String.class);
    }

    public boolean setUsername(String username) {
        return setUsername(username, OnThread.CURRENT);
    }

    public boolean setUsername(String username, OnThread runOnThread) {
        return setAndNotify(AccountProperty.USERNAME, username, runOnThread);
    }

    public String getPassword() {
        return authenticator.getResource(AccountProperty.PASSWORD, String.class);
    }

    public boolean setPassword(String password) {  // triggers sync in later APIs (Android 6)
        return setPassword(password, OnThread.CURRENT);
    }

    public boolean setPassword(String password, OnThread runOnThread) {  // triggers sync in later APIs (Android 6)
        return setAndNotify(AccountProperty.PASSWORD, password, runOnThread);
    }

    public Boolean getPasswordVisibility() {
        return authenticator.getResource(AccountProperty.PASSWORD_VISIBILITY, Boolean.class);
    }

    public boolean setPasswordVisibility(Boolean passwordVisibility) {
        return setAdminPermissions(passwordVisibility, OnThread.CURRENT);
    }

    public boolean setPasswordVisibility(Boolean passwordVisibility, OnThread runOnThread) {
        return setAndNotify(AccountProperty.PASSWORD_VISIBILITY, passwordVisibility, runOnThread);
    }

    public String getApiUrl() {
        return authenticator.getResource(AccountProperty.API_URL, String.class);
    }

    public boolean setApiUrl(String apiUrl) {
        return setApiUrl(apiUrl, OnThread.CURRENT);
    }

    public boolean setApiUrl(String apiUrl, OnThread runOnThread) {
        return setAndNotify(AccountProperty.API_URL, apiUrl, runOnThread);
    }

    public String getApiBaseUrl() {
        return authenticator.getResource(AccountProperty.API_BASE_URL, String.class);
    }

    @NonNull
    public Version getApiVersion() {
        return authenticator.getResource(AccountProperty.VERSION, Version.class);
    }

    public boolean setApiVersion(Api newApi) {
        return setApiVersion(newApi, OnThread.CURRENT);
    }

    public boolean setApiVersion(Api newApi, OnThread runOnThread) {
        Version newVersion = newApi == null ? null : newApi.toVersion();
        return setAndNotify(AccountProperty.VERSION, newVersion, runOnThread);
    }

    @NonNull
    public CertHandlingStrategy getCertHandlingStrategy() {
        return authenticator.getResource(AccountProperty.CERT_HANDLING_STRATEGY, CertHandlingStrategy.class);
    }

    public boolean setCertHandlingStrategy(CertHandlingStrategy certHandlingStrategy) {
        return setCertHandlingStrategy(certHandlingStrategy, OnThread.CURRENT);
    }

    public boolean setCertHandlingStrategy(CertHandlingStrategy certHandlingStrategy, OnThread runOnThread) {
        return setAndNotify(AccountProperty.CERT_HANDLING_STRATEGY, certHandlingStrategy, runOnThread);
    }

    public Boolean hasAdminPermissions() {
        return authenticator.getResource(AccountProperty.HAS_ADMIN_PERMISSIONS, Boolean.class);
    }

    public boolean setAdminPermissions(Boolean hasAdminPermissions) {
        return setAdminPermissions(hasAdminPermissions, OnThread.CURRENT);
    }

    public boolean setAdminPermissions(Boolean hasAdminPermissions, OnThread runOnThread) {
        return setAndNotify(AccountProperty.HAS_ADMIN_PERMISSIONS, hasAdminPermissions, runOnThread);
    }

    @NonNull
    public Cert[] getCertificateChain() {
        return authenticator.getResource(AccountProperty.CERTIFICATE_CHAIN, Cert[].class);
    }

    public boolean setCertificateChain(Cert[] certChain) {
        return setCertificateChain(certChain, OnThread.CURRENT);
    }

    public boolean setCertificateChain(Cert[] certChain, OnThread runOnThread) {
        return setAndNotify(AccountProperty.CERTIFICATE_CHAIN, certChain, runOnThread);
    }

    @NonNull
    public String getValidHostnames() {
        return authenticator.getResource(AccountProperty.VALID_HOSTNAMES, String.class);
    }

    @NonNull
    public String[] getValidHostnameList() {
        return authenticator.getResource(AccountProperty.VALID_HOSTNAME_LIST, String[].class);
    }

    public boolean setValidHostnameList(String[] hostnameList) {
        return setValidHostnameList(hostnameList, OnThread.CURRENT);
    }

    public boolean setValidHostnameList(String[] hostnameList, OnThread runOnThread) {
        return setAndNotify(AccountProperty.VALID_HOSTNAME_LIST, hostnameList, runOnThread);
    }

    @NonNull
    public Boolean isCustomCertificateLocation() {
        return authenticator.getResource(AccountProperty.CUSTOM_CERTIFICATE_LOCATION, Boolean.class);
    }

    public boolean setCustomCertificateLocation(Boolean customCertificateLocation) {
        return setCustomCertificateLocation(customCertificateLocation, OnThread.CURRENT);
    }

    public boolean setCustomCertificateLocation(Boolean customCertificateLocation, OnThread runOnThread) {
        return setAndNotify(AccountProperty.CUSTOM_CERTIFICATE_LOCATION, customCertificateLocation, runOnThread);
    }
}
