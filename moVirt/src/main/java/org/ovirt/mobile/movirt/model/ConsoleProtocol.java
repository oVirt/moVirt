package org.ovirt.mobile.movirt.model;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.TreeSet;

public enum ConsoleProtocol {
    SPICE,
    VNC;

    @NonNull
    public static ConsoleProtocol mapProtocol(String display) {
        try {
            return valueOf(display.toUpperCase());
        } catch (Exception e) {
            // not particularly nice but same behavior as on the webadmin/userportal
            return VNC;
        }
    }

    public static TreeSet<ConsoleProtocol> getProtocolTypes(List<Console> consoles) {
        TreeSet<ConsoleProtocol> result = new TreeSet<>(); // sorted, so that SPICE is first

        for (Console console : consoles) {
            result.add(console.getProtocol());
        }

        return result;
    }

    public String getProtocol() {
        return super.toString().toLowerCase();
    }
}

