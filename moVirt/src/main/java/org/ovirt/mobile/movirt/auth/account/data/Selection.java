package org.ovirt.mobile.movirt.auth.account.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Selection implements Parcelable {

    private static final int APPEND_LENGTH_LIMIT = 10;

    protected final MovirtAccount account;

    protected final List<String> appendArgs;

    protected final String clusterId;

    protected Selection() {
        this(null, SelectedCluster.NOT_SELECTED);
    }

    public Selection(MovirtAccount account, String... appendList) {
        this(account, SelectedCluster.NOT_SELECTED, appendList);
    }

    public Selection(MovirtAccount account, SelectedCluster selectedCluster, String... appendList) {
        ObjectUtils.requireNotNull(selectedCluster, "selectedCluster");
        this.account = account;
        this.clusterId = selectedCluster.clusterId;
        appendArgs = Collections.unmodifiableList(ObjectUtils.asNonEmptyStringList(appendList));
    }

    public boolean isAccount(MovirtAccount account) {
        return this.account != null ? this.account.equals(account) : account == null;
    }

    public boolean isAllAccounts() {
        return account == null;
    }

    public boolean isOneAccount() {
        return account != null;
    }

    public MovirtAccount getAccount() {
        return account;
    }

    public String getAccountId() {
        return account == null ? null : account.getId();
    }

    public boolean isNotCluster() {
        return clusterId == null;
    }

    public boolean isCluster() {
        return clusterId != null;
    }

    public boolean isCluster(String clusterId) {
        return this.clusterId != null ? this.clusterId.equals(clusterId) : clusterId == null;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getDescription(String... appendList) {
        List<String> toProcess = ObjectUtils.asNonEmptyStringList(appendList);
        toProcess.addAll(appendArgs);
        final Iterator<String> it = toProcess.iterator();

        StringBuilder sb = new StringBuilder(account == null ?
                "All" : (it.hasNext() ? ObjectUtils.limitLength(account.getName(), APPEND_LENGTH_LIMIT, true) : account.getName()));

        while (it.hasNext()) {
            String toAppend = it.next();
            sb.append('/').append(it.hasNext() ? ObjectUtils.limitLength(toAppend, APPEND_LENGTH_LIMIT, true) : toAppend);
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Selection)) return false;

        Selection selection = (Selection) o;

        if (account != null ? !account.equals(selection.account) : selection.account != null)
            return false;
        return appendArgs != null ? appendArgs.equals(selection.appendArgs) : selection.appendArgs == null;
    }

    @Override
    public int hashCode() {
        int result = account != null ? account.hashCode() : 0;
        result = 31 * result + (appendArgs != null ? appendArgs.hashCode() : 0);
        return result;
    }

    public Selection(Parcel in) {
        this.appendArgs = in.createStringArrayList();
        this.clusterId = in.readString();

        if (in.readInt() < 0) {
            account = null;
        } else {
            account = new MovirtAccount(in);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(appendArgs);
        dest.writeString(clusterId);

        if (account == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(0);
            account.writeToParcel(dest, flags);
        }
    }

    public static final Parcelable.Creator<Selection> CREATOR = new Parcelable.Creator<Selection>() {
        public Selection createFromParcel(Parcel source) {
            return new Selection(source);
        }

        public Selection[] newArray(int size) {
            return new Selection[size];
        }
    };

    public static class SelectedCluster {
        public static final SelectedCluster NOT_SELECTED = new SelectedCluster((String) null);
        public final String clusterId;

        SelectedCluster(String clusterId) {
            this.clusterId = clusterId;
        }

        public SelectedCluster(Cluster cluster) {
            this.clusterId = cluster == null ? null : cluster.getId();
        }
    }
}
