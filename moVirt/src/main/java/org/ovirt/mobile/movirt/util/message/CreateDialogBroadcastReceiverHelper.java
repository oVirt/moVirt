package org.ovirt.mobile.movirt.util.message;

import android.app.DialogFragment;
import android.app.FragmentManager;

import org.ovirt.mobile.movirt.ui.dialogs.ErrorDialogFragment;
import org.ovirt.mobile.movirt.ui.dialogs.ImportCertificateDialogFragment;

public class CreateDialogBroadcastReceiverHelper {
    private static final String ERROR_DIALOG_TAG = "ERROR_DIALOG_TAG";
    private static final String CERTIFICATE_DIALOG_TAG = "CERTIFICATE_DIALOG_TAG";

    public static void showErrorDialog(FragmentManager manager, String reason, boolean repeatedMinorError) {
        if (!repeatedMinorError) {
            DialogFragment dialogFragment = ErrorDialogFragment.newInstance(reason);
            dialogFragment.show(manager, ERROR_DIALOG_TAG);
        }
    }

    public static void showCertificateDialog(FragmentManager manager, String reason, boolean startActivity) {
        if (manager.findFragmentByTag(CERTIFICATE_DIALOG_TAG) == null) {
            DialogFragment importCertificateDialog =
                    ImportCertificateDialogFragment.newRestCaInstance(reason, startActivity);
            importCertificateDialog.show(manager, CERTIFICATE_DIALOG_TAG);
        }
    }
}
