package org.ovirt.mobile.movirt.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.MainSettingsActivity_;
import org.ovirt.mobile.movirt.ui.account.AddAccountActivity_;
import org.ovirt.mobile.movirt.ui.account.EditAccountsActivity_;

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
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // navigate into the Add AccountActivity settings
                    Intent i = new Intent(getActivity(), MainSettingsActivity_.class);
                    startActivity(i);
                    Intent j = new Intent(getActivity(), EditAccountsActivity_.class);
                    startActivity(j);
                    Intent k = new Intent(getActivity(),
                            AddAccountActivity_.class);
                    startActivity(k);
                })
                .setNegativeButton(android.R.string.no, null);
        return builder.create();
    }
}
