package org.ovirt.mobile.movirt.ui.auth.certificatemanagement.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.properties.property.CertLocation;
import org.ovirt.mobile.movirt.ui.dialogs.ListenerDialogFragment;

public class CertificateLocationChangeDialog extends ListenerDialogFragment<CertificateLocationChangeDialog.ChangeLocationTypeListener> {

    public static CertificateLocationChangeDialog newInstance(CertLocation certLocation, String actionString, String infoString) {
        CertificateLocationChangeDialog fragment = new CertificateLocationChangeDialog();
        Bundle args = new Bundle();
        args.putString("certLocation", certLocation.name());
        args.putString("infoString", infoString);
        args.putString("actionString", actionString);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CertLocation certLocation = CertLocation.fromString(getArguments().getString("certLocation"));
        String infoString = getArguments().getString("infoString");
        String actionString = getArguments().getString("actionString");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        StringBuilder message = new StringBuilder();

        if (!TextUtils.isEmpty(infoString)) {
            message.append(infoString).append("\n\n");
        }

        message.append(getString(R.string.dialog_confirm_message, actionString));

        builder.setTitle(android.R.string.dialog_alert_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, (dialog, which) ->
                        getListener().onLocationChange(certLocation))
                .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.cancel());
        return builder.create();
    }

    public interface ChangeLocationTypeListener {
        void onLocationChange(CertLocation certLocation);
    }
}
