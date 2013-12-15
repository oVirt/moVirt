package org.ovirt.mobile.movirt;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultString;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface AppPrefs {
    String endpoint();

    @DefaultString("admin")
    String username();

    String password();
}