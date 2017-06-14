package org.ovirt.mobile.movirt.auth.properties;

import android.accounts.AccountManager;
import android.text.TextUtils;

import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.auth.properties.property.CertLocation;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
import org.ovirt.mobile.movirt.util.JsonUtils;
import org.ovirt.mobile.movirt.util.ObjectUtils;

public class AccountPropertiesRW {

    private final AccountManager accountManager;

    private final MovirtAccount account;

    private volatile boolean destroyed;

    public AccountPropertiesRW(AccountManager accountManager, MovirtAccount account) {
        ObjectUtils.requireAllNotNull(accountManager, account);
        this.accountManager = accountManager;
        this.account = account;
    }

    public MovirtAccount getAccount() {
        return account;
    }

    public void destroy() {
        destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * @throws IllegalArgumentException if property is not settable
     * @throws AccountDeletedException  if account does not exist
     */
    public void setResource(AccountProperty property, Object object) throws AccountDeletedException {
        if (destroyed) {
            throw new AccountDeletedException();
        }
        switch (property) {
            case AUTH_TOKEN:
                if (object == null) {
                    accountManager.invalidateAuthToken(property.getPackageKey(),
                            accountManager.peekAuthToken(account.getAccount(), property.getPackageKey()));
                }
                accountManager.setAuthToken(account.getAccount(), property.getPackageKey(), PropertyUtils.convertToString(object));
                break;
            case PEEK_AUTH_TOKEN:
            case FUTURE_AUTH_TOKEN:
                throw new IllegalArgumentException(property.name() + " cannot be set! Use AUTH_TOKEN.");
            case PASSWORD:
                accountManager.setPassword(account.getAccount(), PropertyUtils.convertToString(object)); // triggers sync in later APIs (Android 6)
                break;
            case USERNAME:
            case API_URL:
            case VERSION:
            case CERT_HANDLING_STRATEGY:
            case HAS_ADMIN_PERMISSIONS:
            case CERTIFICATE_CHAIN:
            case VALID_HOSTNAME_LIST:
            case CERTIFICATE_LOCATION:
            case FIRST_LOGIN:
                accountManager.setUserData(account.getAccount(), property.getPackageKey(), PropertyUtils.convertToString(object));
                break;
            default:
                throw new IllegalArgumentException(property.name() + " cannot be set!");
        }
    }

    @SuppressWarnings("unchecked")
    public <E> E getResource(AccountProperty property, Class<E> clazz) throws AccountDeletedException {
        return (E) getResource(property);
    }

    /**
     * @throws AccountDeletedException if account does not exist
     */
    public Object getResource(AccountProperty property) throws AccountDeletedException {
        if (destroyed) {
            throw new AccountDeletedException();
        }
        switch (property) {
            case AUTH_TOKEN: // fallback to non blocking peek, used exclusively by AccountPropertiesManager.setAndNotify
            case PEEK_AUTH_TOKEN:
                return accountManager.peekAuthToken(account.getAccount(), AccountProperty.AUTH_TOKEN.getPackageKey());
            case FUTURE_AUTH_TOKEN:
                return accountManager.getAuthToken(account.getAccount(), AccountProperty.AUTH_TOKEN.getPackageKey(), null, false, null, null);
            case PASSWORD:
                return accountManager.getPassword(account.getAccount());
            case USERNAME:
            case API_URL:
                return read(property);
            case API_BASE_URL:
                String baseUrl = read(AccountProperty.API_URL);
                return baseUrl == null ? null : baseUrl.replaceFirst("/api$", "");
            case VERSION:
                return getApiVersion(property);
            case CERT_HANDLING_STRATEGY:
                return CertHandlingStrategy.fromString(read(property));
            case CERTIFICATE_LOCATION:
                return CertLocation.fromString(read(property));
            case HAS_ADMIN_PERMISSIONS:
                return read(property, false);
            case FIRST_LOGIN:
                return read(property, true);
            case CERTIFICATE_CHAIN:
                return getCertificateChain(property);
            case VALID_HOSTNAMES:
                return PropertyUtils.catenateToCsv(getValidHostnames(AccountProperty.VALID_HOSTNAME_LIST));
            case VALID_HOSTNAME_LIST:
                return getValidHostnames(property);
            default:
                return null;
        }
    }

    private Version getApiVersion(AccountProperty property) {
        Version result = readObject(property, Version.class);
        if (result == null) {
            result = Version.V3;
        }
        return result;
    }

    private String[] getValidHostnames(AccountProperty property) {
        String[] result = readObject(property, String[].class);
        return (result == null) ? new String[]{} : result;
    }

    private Cert[] getCertificateChain(AccountProperty property) {
        Cert[] result = readObject(property, Cert[].class);
        return (result == null) ? new Cert[]{} : result;
    }

    private <T> T readObject(AccountProperty property, Class<T> clazz) {
        try {
            return JsonUtils.stringToObject(read(property), clazz);
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean read(AccountProperty property, boolean defRes) {
        String res = accountManager.getUserData(account.getAccount(), property.getPackageKey());
        if (TextUtils.isEmpty(res)) {
            return defRes;
        }

        return Boolean.valueOf(res);
    }

    private String read(AccountProperty property) {
        return accountManager.getUserData(account.getAccount(), property.getPackageKey());
    }
}
