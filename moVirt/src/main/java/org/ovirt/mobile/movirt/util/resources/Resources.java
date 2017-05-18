package org.ovirt.mobile.movirt.util.resources;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.R;

@EBean
public class Resources extends StringResources {

    public String getMissingAccountsPermissionError() {
        return getString(R.string.missing_accounts_permission_error);
    }

    public String getHost() {
        return getString(R.string.host_path);
    }

    public String getStorageDomain() {
        return getString(R.string.storage_domain_path);
    }

    public String getVm() {
        return getString(R.string.vm_path);
    }

    public String getSnapshot() {
        return getString(R.string.snapshot_path);
    }
}
