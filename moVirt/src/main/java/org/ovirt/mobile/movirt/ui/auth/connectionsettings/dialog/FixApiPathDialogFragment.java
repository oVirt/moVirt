package org.ovirt.mobile.movirt.ui.auth.connectionsettings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.ConnectionSettingsActivity;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.LoginInfo;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Dialog asks user to use default API path in URL
 * Created by NoiseDoll on 20.08.2015.
 */
public class FixApiPathDialogFragment extends DialogFragment {

    public static FixApiPathDialogFragment newInstance(String header, LoginInfo loginInfo) {
        FixApiPathDialogFragment fragment = new FixApiPathDialogFragment();
        Bundle args = new Bundle();
        args.putString("header", header);
        args.putParcelable("loginInfo", loginInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LoginInfo loginInfo = getArguments().getParcelable("loginInfo");
        final String header = getArguments().getString("header");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (header == null) {
            builder.setTitle(android.R.string.dialog_alert_title);
        } else {
            builder.setTitle(header);
        }

        builder.setMessage(R.string.account_dialog_missing_api_message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    if (loginInfo != null) {
                        loginInfo.endpoint = fixUrl(loginInfo.endpoint);
                        ConnectionSettingsActivity activity = (ConnectionSettingsActivity) getActivity();
                        activity.displayApiUrl(loginInfo.endpoint);
                        activity.getPresenter().login(loginInfo);
                    }
                })
                .setNegativeButton(android.R.string.no, (dialog, which) ->
                        ((ConnectionSettingsActivity) getActivity()).getPresenter().login(loginInfo));
        return builder.create();
    }

    private String fixUrl(String endpoint) {
        try {
            return new URL(new URL(endpoint), getString(R.string.default_api_path)).toString();
        } catch (MalformedURLException ignored) {
            return endpoint;
        }
    }
}
