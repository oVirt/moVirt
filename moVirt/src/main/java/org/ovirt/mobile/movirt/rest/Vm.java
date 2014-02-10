package org.ovirt.mobile.movirt.rest;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonValue;

import static org.ovirt.mobile.movirt.model.Vm.Status.*;

@JsonIgnoreProperties(ignoreUnknown = true)
class Vm implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Vm> {
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

    public org.ovirt.mobile.movirt.model.Vm toEntity() {
        org.ovirt.mobile.movirt.model.Vm vm = new org.ovirt.mobile.movirt.model.Vm();
        vm.setId(id);
        vm.setName(name);
        vm.setStatus(mapStatus(getStatus()));

        return vm;
    }

    private static String mapStatus(String status) {
//        switch (status.toLowerCase()) {
//            case "up":
//                return UP;
//            case "down":
//                return DOWN;
//            default:
//                return UNKNOWN;
//        }
        return status.toLowerCase();
    }
}
