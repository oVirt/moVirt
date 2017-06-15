package org.ovirt.mobile.movirt.auth.account.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ActiveSelection extends Selection {
    public static final ActiveSelection ALL_ACTIVE = new ActiveSelection();

    private final transient String clusterName;

    public ActiveSelection() {
        this(null, null, null);
    }

    public ActiveSelection(MovirtAccount account) {
        this(account, null, null);
    }

    public ActiveSelection(MovirtAccount account, String clusterId) {
        this(account, clusterId, null);
    }

    public ActiveSelection(MovirtAccount account, String clusterId, String clusterName) {
        super(account, new SelectedCluster(clusterId), clusterName);
        this.clusterName = clusterName;
    }

    public boolean isClusterName(String clusterName) {
        return this.clusterName != null ? this.clusterName.equals(clusterName) : clusterName == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActiveSelection)) return false;
        if (!super.equals(o)) return false;

        ActiveSelection that = (ActiveSelection) o;

        return clusterName != null ? clusterName.equals(that.clusterName) : that.clusterName == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (clusterName != null ? clusterName.hashCode() : 0);
        return result;
    }

    public ActiveSelection(Parcel in) {
        super(in);
        this.clusterName = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(clusterName);
    }

    public static final Parcelable.Creator<ActiveSelection> CREATOR = new Parcelable.Creator<ActiveSelection>() {
        public ActiveSelection createFromParcel(Parcel source) {
            return new ActiveSelection(source);
        }

        public ActiveSelection[] newArray(int size) {
            return new ActiveSelection[size];
        }
    };
}
