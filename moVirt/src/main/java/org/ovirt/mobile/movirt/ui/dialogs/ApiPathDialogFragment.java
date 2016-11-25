package org.ovirt.mobile.movirt.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.auth.AuthenticatorActivity;

/**
 * Dialog asks user to use default API path in URL
 * Created by NoiseDoll on 20.08.2015.
 */
public class ApiPathDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(android.R.string.dialog_alert_title)
                .setMessage(R.string.account_dialog_missing_api_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((AuthenticatorActivity) getActivity()).fixUrlAndLogin();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((AuthenticatorActivity) getActivity()).finishLogin();
                    }
                });
        return builder.create();
    }
}
