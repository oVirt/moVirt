package org.ovirt.mobile.movirt;

import org.androidannotations.annotations.sharedpreferences.DefaultRes;
import org.androidannotations.annotations.sharedpreferences.SharedPref;
import org.ovirt.mobile.movirt.R;

@SharedPref(SharedPref.Scope.APPLICATION_DEFAULT)
public interface AppPrefs {
    @DefaultRes(R.string.default_endpoint)
    String endpoint();

    @DefaultRes(R.string.default_username)
    String username();

    @DefaultRes(R.string.default_password)
    String password();
}