package org.ovirt.mobile.movirt.util.message;

/**
 * All activities need to listen for broadcasts Broadcasts.ERROR_MESSAGE and Broadcasts.REST_CA_FAILURE
 * and call appropriate method in CreateDialogBroadcastReceiverHelper to present dialogs to the user
 */
public interface CreateDialogBroadcastReceiver {
    void showCertificateDialog(String reason);

    void showErrorDialog(String reason, boolean repeatedFailure);
}
