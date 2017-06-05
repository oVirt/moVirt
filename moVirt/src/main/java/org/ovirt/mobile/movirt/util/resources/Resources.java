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
}
