package org.ovirt.mobile.movirt.util.resources;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.R;

@EBean
public class Resources extends StringResources {

    public String getTriggers() {
        return getString(R.string.title_activity_edit_triggers);
    }

    public String getMissingAccountsPermissionError() {
        return getString(R.string.missing_accounts_permission_error);
    }

    public String getNoConsoleFileError(String error) {
        return getString(R.string.no_console_file_error, error);
    }

    public String getNoConsoleClientError() {
        return getString(R.string.no_console_client_error);
    }

    public String getCreatedDuplicateTriggerError() {
        return getString(R.string.create_duplicate_trigger_error);
    }

    public String getUpdateDuplicateTriggerError() {
        return getString(R.string.update_duplicate_trigger_error);
    }
}
