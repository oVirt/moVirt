package org.ovirt.mobile.movirt.rest;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cluster implements Parcelable {
    // public for json mapping
    public String name;

    public String getName() {
        return name;
    }

    public Cluster(Parcel parcel) {
        name = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
    }
}
