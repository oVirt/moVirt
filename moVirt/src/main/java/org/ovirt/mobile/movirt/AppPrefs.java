package org.ovirt.mobile.movirt;

import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface AppPrefs {
    String endpoint();

    @DefaultString("admin")
    String username();

    String password();
}