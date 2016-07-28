package org.ovirt.mobile.movirt.ui.dialogs;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.model.CaCert;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.CertHandlingStrategy;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Dialog to show error messages with function to copy message
 * Created by Nika on 18.08.2015.
 */
public class ErrorDialogFragment extends DialogFragment {

    public static ErrorDialogFragment newInstance(String errorMessage) {
        ErrorDialogFragment fragment = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", errorMessage);
        fragment.setArguments(args);
        return fragment;
    }

    public static ErrorDialogFragment newInstance(Context context, MovirtAuthenticator authenticator,
                                                  ProviderFacade provider, String reason) {
        String token = AccountManager.get(context).peekAuthToken(
                MovirtAuthenticator.MOVIRT_ACCOUNT,
                MovirtAuthenticator.AUTH_TOKEN_TYPE);
        if (token == null) {
            token = "Token is missing!\n\tauthentication failed";
        }
        String apiUrl = "";
        StringBuilder certificate = new StringBuilder();
        if (authenticator.getApiUrl() != null) {
            apiUrl = authenticator.getApiUrl();
            URL url;
            try {
                url = new URL(apiUrl);
                if (url.getProtocol().equalsIgnoreCase("https")) {
                    certificate.append("\nCertificate strategy: ")
                            .append(authenticator.getCertHandlingStrategy().toString());
                }
                if (authenticator.getCertHandlingStrategy() == CertHandlingStrategy.TRUST_CUSTOM) {
                    if (provider.query(CaCert.class).all().size() > 0) {
                        certificate.append("\n\tcertificate is downloaded and stored");
                    } else {
                        certificate.append("\n\tcertificate is missing!");
                    }
                }
            } catch (MalformedURLException e) {
                apiUrl = "url is malformed\n\t" + e.getMessage();
            }
        } else {
            apiUrl = "url is missing!";
        }
        String errorMessage = context.getString(R.string.rest_req_failed) +
                "\nConnection details:\nAPI URL: " + apiUrl +
                "\nUsername: " + authenticator.getUserName() +
                "\nToken: " + token +
                certificate.toString() +
                "\n\nError details:\n" + reason;

        return newInstance(errorMessage);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getArguments().getString("message");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_error, null);
        TextView textView = (TextView) view.findViewById(R.id.errorDialogText);
        textView.setText(message);
        builder.setTitle(android.R.string.dialog_alert_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null);
        return builder.create();
    }
}