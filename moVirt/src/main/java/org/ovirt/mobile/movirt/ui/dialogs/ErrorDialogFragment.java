package org.ovirt.mobile.movirt.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.ovirt.mobile.movirt.R;

/**
 * Dialog to show error messages with function to copy message
 * Created by NoiseDoll on 18.08.2015.
 */
public class ErrorDialogFragment extends DialogFragment {

    public static ErrorDialogFragment newInstance(String errorMessage) {
        ErrorDialogFragment fragment = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", errorMessage);
        fragment.setArguments(args);
        return fragment;
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