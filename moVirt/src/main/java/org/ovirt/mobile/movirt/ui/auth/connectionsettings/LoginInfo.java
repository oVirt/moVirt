package org.ovirt.mobile.movirt.ui.auth.connectionsettings;

import android.os.Parcel;
import android.os.Parcelable;

public class LoginInfo implements Parcelable {
    public String endpoint;
    public String username;
    public String password;
    public boolean adminPrivileges;

    public LoginInfo(String endpoint, String username, String password, boolean adminPrivileges) {
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;
        this.adminPrivileges = adminPrivileges;
    }

    public LoginInfo(Parcel parcel) {
        this.endpoint = parcel.readString();
        this.username = parcel.readString();
        this.password = parcel.readString();
        this.adminPrivileges = parcel.readInt() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(endpoint);
        dest.writeString(username);
        dest.writeString(password);
        dest.writeInt(adminPrivileges ? 1 : 0);
    }

    public static final Creator<LoginInfo> CREATOR = new Creator<LoginInfo>() {
        public LoginInfo createFromParcel(Parcel source) {
            return new LoginInfo(source);
        }

        public LoginInfo[] newArray(int size) {
            return new LoginInfo[size];
        }
    };
}
