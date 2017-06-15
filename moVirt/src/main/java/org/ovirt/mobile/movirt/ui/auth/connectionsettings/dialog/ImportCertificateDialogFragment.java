package org.ovirt.mobile.movirt.ui.auth.connectionsettings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.ui.auth.certificatemanagement.CertificateManagementActivity;
import org.ovirt.mobile.movirt.ui.auth.certificatemanagement.CertificateManagementActivity_;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.ConnectionSettingsActivity;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.ConnectionSettingsActivity_;
import org.ovirt.mobile.movirt.util.ObjectUtils;

/**
 * Dialog asking user to import certificate.
 * Created by Nika on 20.08.2015.
 */
public class ImportCertificateDialogFragment extends DialogFragment {

    public static ImportCertificateDialogFragment newInstance(@Nullable String additionalMessage, MovirtAccount account, String apiUrl) {
        ObjectUtils.requireNotNull(account, "account");
        ImportCertificateDialogFragment fragment = new ImportCertificateDialogFragment();

        Bundle args = new Bundle();
        if (additionalMessage != null) {
            args.putString("additionalMessage", additionalMessage);
        }
        args.putString("apiUrl", apiUrl);
        args.putParcelable("account", account);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String additionalMessage = getArguments().getString("additionalMessage");
        final String apiUrl = getArguments().getString("apiUrl");

        final MovirtAccount account = getArguments().getParcelable("account");
        StringBuilder message =
                new StringBuilder(getString(R.string.dialog_certificate_missing_start));
        if (additionalMessage != null) {
            message.append('\n').append(additionalMessage).append('\n');
        }
        message.append(getString(R.string.dialog_certificate_missing_end));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(account.getName())
                .setMessage(message.toString())
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                    if (getActivity() instanceof CertificateManagementActivity) {
                        // no action even if user is editing different accounts
                        return;
                    }

                    boolean isConnSettings = getActivity() instanceof ConnectionSettingsActivity;
                    if (isConnSettings && !((ConnectionSettingsActivity) getActivity()).getPresenter().isInstanceOfAccount(account)) {
                        // no action even if user is editing different accounts
                        return;
                    }

                    if (!isConnSettings) {
                        final Intent i = new Intent(getActivity(), ConnectionSettingsActivity_.class);
                        i.putExtra(Constants.ACCOUNT_KEY, account);
                        startActivity(i);
                    }

                    final Intent j = new Intent(getActivity(), CertificateManagementActivity_.class);
                    j.putExtra(Constants.ACCOUNT_KEY, account);
                    j.putExtra(CertificateManagementActivity.LOAD_CA_FROM, apiUrl);
                    startActivity(j);
                })
                .setNegativeButton(android.R.string.no, null);
        return builder.create();
    }
}
