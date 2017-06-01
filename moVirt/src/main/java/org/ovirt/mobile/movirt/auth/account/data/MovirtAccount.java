package org.ovirt.mobile.movirt.auth.account.data;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.ovirt.mobile.movirt.util.ObjectUtils;

public class MovirtAccount implements Parcelable, Comparable<MovirtAccount> {
    private final String id;

    private final Account account;

    public MovirtAccount(String id, Account account) {
        ObjectUtils.requireAllNotNull(id, account);
        this.account = account;
        this.id = id;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public Account getAccount() {
        return account;
    }

    @NonNull
    public String getName() {
        return account.name;
    }

    @NonNull
    public String getType() {
        return account.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovirtAccount)) return false;

        MovirtAccount account = (MovirtAccount) o;

        return id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(MovirtAccount account) {
        return this.account.name.compareTo(account.account.name);
    }

    public MovirtAccount(Parcel in) throws IllegalArgumentException {
        this.id = in.readString();
        if (id == null) {
            throw new IllegalArgumentException("Cannot be serialized from this parcel");
        }
        account = new Account(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        account.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<MovirtAccount> CREATOR = new Parcelable.Creator<MovirtAccount>() {
        public MovirtAccount createFromParcel(Parcel source) {
            return new MovirtAccount(source);
        }

        public MovirtAccount[] newArray(int size) {
            return new MovirtAccount[size];
        }
    };

    public String toString() {
        return "Account {name=" + account.name + ", id=" + id + ", type=" + account.type + "}";
    }
}
