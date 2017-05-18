package org.ovirt.mobile.movirt.util.message;

import android.app.DialogFragment;
import android.app.FragmentManager;

import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.dialog.ImportCertificateDialogFragment;
import org.ovirt.mobile.movirt.ui.dialogs.ErrorDialogFragment;

public class CreateDialogBroadcastReceiverHelper {
    private static final String ERROR_DIALOG_TAG = "ERROR_DIALOG_TAG";
    private static final String CERTIFICATE_DIALOG_TAG = "CERTIFICATE_DIALOG_TAG";

    public static void showErrorDialog(FragmentManager manager, String header, String reason) {
        DialogFragment dialogFragment = ErrorDialogFragment.newInstance(header, reason);
        dialogFragment.show(manager, ERROR_DIALOG_TAG);
    }

    public static void showCertificateDialog(FragmentManager manager, MovirtAccount account, String reason, String apiUrl) {
        if (manager.findFragmentByTag(CERTIFICATE_DIALOG_TAG) == null) {
            DialogFragment importCertificateDialog = ImportCertificateDialogFragment.newInstance(reason, account, apiUrl);
            importCertificateDialog.show(manager, CERTIFICATE_DIALOG_TAG);
        }
    }
}
