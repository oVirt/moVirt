package org.ovirt.mobile.movirt.auth.account.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ActiveSelection extends Selection {
    public static final ActiveSelection ALL_ACTIVE = new ActiveSelection();

    public ActiveSelection() {
        super();
    }

    public ActiveSelection(MovirtAccount account) {
        super(account);
    }

    public ActiveSelection(MovirtAccount account, String clusterId) {
        super(account, clusterId);
    }

    public ActiveSelection(MovirtAccount account, String clusterId, String clusterName) {
        super(account, clusterId, clusterName);
    }

    public ActiveSelection(Parcel in) {
        super(in);
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
