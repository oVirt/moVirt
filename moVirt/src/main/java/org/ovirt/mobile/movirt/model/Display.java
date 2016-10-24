package org.ovirt.mobile.movirt.model;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.TreeSet;

public enum Display {
    SPICE,
    VNC;

    public String getProtocol() {
        return toString().toLowerCase();
    }

    @NonNull
    public static Display mapDisplay(String display) {
        try {
            return valueOf(display.toUpperCase());
        } catch (Exception e) {
            // not particularly nice but same behavior as on the webadmin/userportal
            return VNC;
        }
    }

    public static TreeSet<Display> getDisplayTypes(List<Console> consoles) {
        TreeSet<Display> result = new TreeSet<>(); // sorted, so that SPICE is first

        for (Console console : consoles) {
            result.add(console.getDisplayType());
        }

        return result;
    }
}

