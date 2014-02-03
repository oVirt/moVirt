package org.ovirt.mobile.movirt.rest;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Vm implements Parcelable {
    // public for json mapping
    public String id;
    public String name;
    public Status status;

    public Vm() {
    }

    public Vm(Parcel parcel) {
        id = parcel.readString();
        name = parcel.readString();
        status = new Status();
        status.state = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(status.state);
    }

    // status complex object in rest
    public static class Status {
        public String state;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status.state;
    }
}
