package org.ovirt.mobile.movirt.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.AuthenticatorActivity_;

/**
 * Dialog shown on app start up asking user to configure account with connection settings
 * Created by NoiseDoll on 20.08.2015.
 */
public class AccountDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(android.R.string.dialog_alert_title)
                .setMessage(R.string.needs_configuration)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity(),
                                AuthenticatorActivity_.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.no, null);
        return builder.create();
    }
}
