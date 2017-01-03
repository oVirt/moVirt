package org.ovirt.mobile.movirt.auth.properties;

import android.accounts.AccountManagerFuture;
import android.os.Bundle;

import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Types of Account Properties
 */
public enum AccountProperty {
    /**
     * Should be used together with {@link String}. Getter of this property uses PEEK_AUTH_TOKEN, which can be null.
     */
    AUTH_TOKEN,
    /**
     * Should be used together with {@link String}. This property is not settable. Returned token can be null.
     */
    PEEK_AUTH_TOKEN(false),
    /**
     * Should be used together with {@link android.accounts.AccountManagerFuture<android.os.Bundle>}. This property is not settable.
     */
    FUTURE_AUTH_TOKEN(false),
    /**
     * Should be used together {@link Boolean}. This property is not settable.
     */
    ACCOUNT_CONFIGURED(false),
    /**
     * Should be used together {@link Boolean}.
     */
    FIRST_LOGIN,
    /**
     * Should be used together with {@link String}.
     */
    PASSWORD,
    /**
     * Should be used together with {@link Boolean}.
     */
    PASSWORD_VISIBILITY,
    /**
     * Should be used together with {@link String}.
     */
    USERNAME,
    /**
     * Should be used together with {@link String}. See also {@linkplain AccountProperty#getDependentProperties()}
     */
    API_URL,
    /**
     * Should be used together with {@link String}. This property is not settable.
     */
    API_BASE_URL(false),
    /**
     * Should be used together with {@link Version}.
     */
    VERSION,
    /**
     * Should be used together {@link CertHandlingStrategy}.
     */
    CERT_HANDLING_STRATEGY,
    /**
     * Should be used together {@link Boolean}.
     */
    HAS_ADMIN_PERMISSIONS,
    /**
     * Should be used together with {@link Cert Cert[]}.
     */
    CERTIFICATE_CHAIN,
    /**
     * Should be used together with {@link String}. This property is not settable.
     */
    VALID_HOSTNAMES(false),
    /**
     * Should be used together with {@link String String[] }. See also {@linkplain AccountProperty#getDependentProperties()}
     */
    VALID_HOSTNAME_LIST,
    /**
     * Should be used together with {@link Boolean }.
     */
    CUSTOM_CERTIFICATE_LOCATION;

    private boolean settable = true;

    private Set<AccountProperty> dependentProperties = Collections.unmodifiableSet(Collections.<AccountProperty>emptySet());

    private String packageKey = Constants.APP_PACKAGE_DOT + this.name();

    static {
        API_URL.dependentProperties = Collections.unmodifiableSet(EnumSet.of(
                API_BASE_URL));
        VALID_HOSTNAME_LIST.dependentProperties = Collections.unmodifiableSet(EnumSet.of(
                VALID_HOSTNAMES));
    }

    AccountProperty() {
    }

    AccountProperty(boolean settable) {
        this.settable = settable;
    }

    public boolean isSettable() {
        return settable;
    }

    /**
     * Any change on this property will also fire changes in dependent properties
     *
     * @return dependent properties of this property
     */
    public Set<AccountProperty> getDependentProperties() {
        return dependentProperties;
    }

    public String getPackageKey() {
        return packageKey;
    }

    public abstract static class AuthTokenListener implements PropertyChangedListener<String> {
        @Override
        public abstract void onPropertyChange(String authToken);

        @Override
        public AccountProperty getProperty() {
            return AUTH_TOKEN;
        }
    }

    public abstract static class PeekAuthTokenListener implements PropertyChangedListener<String> {
        @Override
        public abstract void onPropertyChange(String authToken);

        @Override
        public AccountProperty getProperty() {
            return PEEK_AUTH_TOKEN;
        }
    }

    public abstract static class FutureAuthTokenListener implements PropertyChangedListener<AccountManagerFuture<Bundle>> {
        @Override
        public abstract void onPropertyChange(AccountManagerFuture<Bundle> authToken);

        @Override
        public AccountProperty getProperty() {
            return FUTURE_AUTH_TOKEN;
        }
    }

    public abstract static class AccountConfiguredListener implements PropertyChangedListener<Boolean> {
        @Override
        public abstract void onPropertyChange(Boolean accountConfigured);

        @Override
        public AccountProperty getProperty() {
            return ACCOUNT_CONFIGURED;
        }
    }

    public abstract static class FirstLoginListener implements PropertyChangedListener<Boolean> {
        @Override
        public abstract void onPropertyChange(Boolean firstLogin);

        @Override
        public AccountProperty getProperty() {
            return FIRST_LOGIN;
        }
    }

    public abstract static class PasswordListener implements PropertyChangedListener<String> {
        @Override
        public abstract void onPropertyChange(String password);

        @Override
        public AccountProperty getProperty() {
            return PASSWORD;
        }
    }

    public abstract static class PasswordVisibilityListener implements PropertyChangedListener<Boolean> {
        @Override
        public abstract void onPropertyChange(Boolean passwordVisibility);

        @Override
        public AccountProperty getProperty() {
            return PASSWORD_VISIBILITY;
        }
    }

    public abstract static class UsernameListener implements PropertyChangedListener<String> {
        @Override
        public abstract void onPropertyChange(String username);

        @Override
        public AccountProperty getProperty() {
            return USERNAME;
        }
    }

    public abstract static class ApiUrlListener implements PropertyChangedListener<String> {
        @Override
        public abstract void onPropertyChange(String apiUrl);

        @Override
        public AccountProperty getProperty() {
            return API_URL;
        }
    }

    public abstract static class ApiBaseUrlListener implements PropertyChangedListener<String> {
        @Override
        public abstract void onPropertyChange(String apiBaseUrl);

        @Override
        public AccountProperty getProperty() {
            return API_BASE_URL;
        }
    }

    public abstract static class VersionListener implements PropertyChangedListener<Version> {
        @Override
        public abstract void onPropertyChange(Version version);

        @Override
        public AccountProperty getProperty() {
            return VERSION;
        }
    }

    public abstract static class CertHandlingStrategyListener implements PropertyChangedListener<CertHandlingStrategy> {
        @Override
        public abstract void onPropertyChange(CertHandlingStrategy certHandlingStrategy);

        @Override
        public AccountProperty getProperty() {
            return CERT_HANDLING_STRATEGY;
        }
    }

    public abstract static class HasAdminPermissionsListener implements PropertyChangedListener<Boolean> {
        @Override
        public abstract void onPropertyChange(Boolean hasAdminPermissions);

        @Override
        public AccountProperty getProperty() {
            return HAS_ADMIN_PERMISSIONS;
        }
    }

    public abstract static class CertificateChainListener implements PropertyChangedListener<Cert[]> {
        @Override
        public abstract void onPropertyChange(Cert[] certificates);

        @Override
        public AccountProperty getProperty() {
            return CERTIFICATE_CHAIN;
        }
    }

    public abstract static class ValidHostnamesListener implements PropertyChangedListener<String> {
        @Override
        public abstract void onPropertyChange(String validHostnames);

        @Override
        public AccountProperty getProperty() {
            return VALID_HOSTNAMES;
        }
    }

    public abstract static class ValidHostnameListListener implements PropertyChangedListener<String[]> {
        @Override
        public abstract void onPropertyChange(String[] validHostnameList);

        @Override
        public AccountProperty getProperty() {
            return VALID_HOSTNAME_LIST;
        }
    }

    public abstract static class CustomCertificateLocationListener implements PropertyChangedListener<Boolean> {
        @Override
        public abstract void onPropertyChange(Boolean customCertificateLocation);

        @Override
        public AccountProperty getProperty() {
            return CUSTOM_CERTIFICATE_LOCATION;
        }
    }
}
