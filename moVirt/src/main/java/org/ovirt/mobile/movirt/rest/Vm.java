package org.ovirt.mobile.movirt.rest;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Vm {
    // public for json mapping
    public String id;
    public String name;
    public Status status;

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

    @Override
    public String toString() {
        return String.format("Vm: name=%s, id=%s, status=%s", name, id, getStatus());
    }
}
