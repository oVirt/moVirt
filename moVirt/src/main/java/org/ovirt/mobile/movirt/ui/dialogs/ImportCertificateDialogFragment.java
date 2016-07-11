package org.ovirt.mobile.movirt.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.AdvancedAuthenticatorActivity;
import org.ovirt.mobile.movirt.ui.AdvancedAuthenticatorActivity_;
import org.ovirt.mobile.movirt.ui.AuthenticatorActivity;
import org.ovirt.mobile.movirt.ui.AuthenticatorActivity_;

/**
 * Dialog asking user to import certificate.
 * Created by Nika on 20.08.2015.
 */
public class ImportCertificateDialogFragment extends DialogFragment {

    public static ImportCertificateDialogFragment newRestCaInstance(@Nullable String additionalMessage,
                                                                    boolean startActivity) {
        ImportCertificateDialogFragment fragment = new ImportCertificateDialogFragment();
        Bundle args = new Bundle();
        if (additionalMessage != null) {
            args.putString("additionalMessage", additionalMessage);
        }
        args.putInt("managementMode", AdvancedAuthenticatorActivity.MODE_REST_CA_MANAGEMENT);
        args.putBoolean("startActivity", startActivity);
        fragment.setArguments(args);
        return fragment;
    }

    public static ImportCertificateDialogFragment newSpiceCaInstance(@Nullable String additionalMessage,
                                                                     long certHandlingStrategyId,
                                                                     String endpoint) {
        ImportCertificateDialogFragment fragment = new ImportCertificateDialogFragment();
        Bundle args = new Bundle();
        if (additionalMessage != null) {
            args.putString("additionalMessage", additionalMessage);
        }
        args.putInt("managementMode", AdvancedAuthenticatorActivity.MODE_SPICE_CA_MANAGEMENT);
        args.putLong("certHandlingStrategyId", certHandlingStrategyId);
        args.putString("endpoint", endpoint);
        args.putBoolean("startActivity", false);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String additionalMessage = getArguments().getString("additionalMessage");
        final int managementMode = getArguments().getInt("managementMode");
        final long certHandlingStrategyId = getArguments().getLong("certHandlingStrategyId");
        final String endpoint = getArguments().getString("endpoint");
        final boolean startActivity = getArguments().getBoolean("startActivity");

        StringBuilder message =
                new StringBuilder(getString(R.string.dialog_certificate_missing_start));
        if (additionalMessage != null) {
            message.append('\n').append(additionalMessage).append('\n');
        }
        message.append(getString(R.string.dialog_certificate_missing_end));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(android.R.string.dialog_alert_title)
                .setMessage(message.toString())
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (managementMode ==
                                AdvancedAuthenticatorActivity.MODE_SPICE_CA_MANAGEMENT) {
                            Intent intent = new Intent(getActivity(),
                                    AdvancedAuthenticatorActivity_.class);
                            intent.putExtra(AdvancedAuthenticatorActivity.MODE, managementMode);
                            intent.putExtra(AdvancedAuthenticatorActivity.CERT_HANDLING_STRATEGY,
                                    certHandlingStrategyId);
                            intent.putExtra(AdvancedAuthenticatorActivity.LOAD_CA_FROM, endpoint);
                            startActivity(intent);
                        } else if (startActivity) {
                            final Intent intent = new Intent(getActivity(),
                                    AuthenticatorActivity_.class);
                            intent.putExtra(AuthenticatorActivity.SHOW_ADVANCED_AUTHENTICATOR, true);
                            startActivity(intent);
                        } else {
                            ((AuthenticatorActivity) getActivity()).btnAdvancedClicked();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null);
        return builder.create();
    }
}
