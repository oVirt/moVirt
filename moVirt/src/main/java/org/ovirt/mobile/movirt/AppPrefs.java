package org.ovirt.mobile.movirt;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultString;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface AppPrefs {
    @DefaultString("http://localhost:8080")
    String endpoint();

    @DefaultString("admin")
    String username();

    @DefaultString("engine")
    String password();
}