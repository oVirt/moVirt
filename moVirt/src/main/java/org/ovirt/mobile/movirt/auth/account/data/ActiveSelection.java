package org.ovirt.mobile.movirt.auth.account.data;

import android.support.annotation.NonNull;

import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class ActiveSelection {
    public static final ActiveSelection ALL_ACTIVE = new ActiveSelection();

    public static final int APPEND_LENGTH_LIMIT = 10;

    private final MovirtAccount account;

    private final String clusterId;

    private final transient String clusterName;

    private ActiveSelection() {
        account = null;
        clusterId = null;
        clusterName = null;
    }

    public ActiveSelection(MovirtAccount account) {
        this(account, null, null);
    }

    public ActiveSelection(MovirtAccount account, String clusterId) {
        this(account, clusterId, null);
    }

    public ActiveSelection(MovirtAccount account, String clusterId, String clusterName) {
        ObjectUtils.requireNotNull(account, "account");
        this.account = account;
        this.clusterId = clusterId;
        this.clusterName = clusterName;
    }

    public boolean isAccount(MovirtAccount account) {
        return this.account != null ? this.account.equals(account) : account == null;
    }

    public boolean isNotCluster() {
        return isCluster(null);
    }

    public boolean isCluster() {
        return clusterId != null;
    }

    public boolean isCluster(String clusterId) {
        return this.clusterId != null ? this.clusterId.equals(clusterId) : clusterId == null;
    }

    public boolean isClusterName(String clusterName) {
        return this.clusterName != null ? this.clusterName.equals(clusterName) : clusterName == null;
    }

    public boolean isAllAccounts() {
        return account == null;
    }

    public MovirtAccount getAccount() {
        return account;
    }

    @NonNull
    public String getAccountName() {
        return account == null ? "" : account.getName();
    }

    public String getDescription(String append) {
        return getDescription(account, clusterName, append);
    }

    public static String getDescription(MovirtAccount account, String clusterName, String... appendList) {
        if (account == null) {
            return "All";
        } else {
            if (clusterName == null) {
                return account.getName();
            } else {
                StringBuilder sb = new StringBuilder();

                sb.append(limitLength(account.getName(), APPEND_LENGTH_LIMIT));

                if (!StringUtils.isEmpty(clusterName)) {
                    sb.append('/').append(clusterName);
                }

                for (int i = 0; i < appendList.length; i++) {
                    String toAppend = appendList[i];
                    if (!StringUtils.isEmpty(toAppend)) {
                        sb.append('/').append(i == appendList.length - 1 ? toAppend : limitLength(toAppend, APPEND_LENGTH_LIMIT));
                    }
                }

                return sb.toString();
            }
        }
    }

    private static String limitLength(String input, int limit) {
        if (input == null) {
            return "";
        } else if (input.length() > limit) {
            return input.substring(0, limit);
        }

        return input;
    }

    public String getDescription() {
        return getDescription(null);
    }

    public String getClusterId() {
        return clusterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActiveSelection)) return false;

        ActiveSelection selection = (ActiveSelection) o;

        if (account != null ? !account.equals(selection.account) : selection.account != null)
            return false;
        if (clusterId != null ? !clusterId.equals(selection.clusterId) : selection.clusterId != null)
            return false;
        return clusterName != null ? clusterName.equals(selection.clusterName) : selection.clusterName == null;
    }

    @Override
    public int hashCode() {
        int result = account != null ? account.hashCode() : 0;
        result = 31 * result + (clusterId != null ? clusterId.hashCode() : 0);
        result = 31 * result + (clusterName != null ? clusterName.hashCode() : 0);
        return result;
    }
}
