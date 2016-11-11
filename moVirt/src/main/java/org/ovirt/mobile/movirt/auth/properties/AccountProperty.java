package org.ovirt.mobile.movirt.auth.properties;

import org.ovirt.mobile.movirt.Constants;

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
     * Should be used together with {@link String}.
     */
    PASSWORD,
    /**
     * Should be used together with {@link String}.
     */
    USERNAME,
    /**
     * Should be used together with {@link String}.
     */
    API_URL,
    /**
     * Should be used together with {@link String}. This property is not settable.
     */
    API_BASE_URL(false),
    /**
     * Should be used together with {@link org.ovirt.mobile.movirt.util.Version}.
     */
    VERSION,
    /**
     * Should be used together {@link org.ovirt.mobile.movirt.ui.CertHandlingStrategy}.
     */
    CERT_HANDLING_STRATEGY,
    /**
     * Should be used together {@link Boolean}.
     */
    HAS_ADMIN_PERMISSIONS,
    /**
     * Should be used together with {@link org.ovirt.mobile.movirt.auth.CaCert org.ovirt.mobile.movirt.auth.CaCert[]}.
     */
    CERTIFICATE_CHAIN;

    private boolean settable = true;

    private String packageKey = Constants.APP_PACKAGE_DOT + this.name();

    AccountProperty() {
    }

    AccountProperty(boolean settable) {
        this.settable = settable;
    }

    public boolean isSettable() {
        return settable;
    }

    public String getPackageKey() {
        return packageKey;
    }
}
