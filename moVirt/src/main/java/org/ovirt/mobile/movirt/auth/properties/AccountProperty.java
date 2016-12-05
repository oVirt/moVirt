package org.ovirt.mobile.movirt.auth.properties;

import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.auth.properties.property.Version;

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
}
