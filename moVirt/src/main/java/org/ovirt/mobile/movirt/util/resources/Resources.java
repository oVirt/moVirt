package org.ovirt.mobile.movirt.util.resources;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.mapping.EntityType;

@EBean
public class Resources extends StringResources {

    public String getTriggers() {
        return getString(R.string.title_activity_edit_triggers);
    }

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

    public String getEvent() {
        return getString(R.string.event_path);
    }

    public String getEntityString(EntityType entityType) {
        switch (entityType) {
            case VM:
                return getVm();
            case HOST:
                return getHost();
            case STORAGE_DOMAIN:
                return getStorageDomain();
            case EVENT:
                return getEvent();
            default:
                return "";
        }
    }
}
