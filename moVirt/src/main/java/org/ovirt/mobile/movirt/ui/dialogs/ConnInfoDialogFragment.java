package org.ovirt.mobile.movirt.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.IconDimension;

import java.util.ArrayList;

public class ConnInfoDialogFragment extends DialogFragment {

    public static ConnInfoDialogFragment newInstance(ArrayList<String> names, ArrayList<String> errors) {
        ConnInfoDialogFragment fragment = new ConnInfoDialogFragment();

        Bundle args = new Bundle();
        args.putStringArrayList("names", names);
        args.putStringArrayList("errors", errors);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<String> names = getArguments().getStringArrayList("names");
        ArrayList<String> errors = getArguments().getStringArrayList("errors");

        if (names == null || errors == null || names.size() != errors.size()) {
            return null;
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.conn_dialog_error, null);

        LinearLayout container = (LinearLayout) view.findViewById(R.id.conn_error_container);

        for (int i = 0; i < names.size(); i++) {
            @SuppressLint("InflateParams")
            View nextError = inflater.inflate(R.layout.conn_dialog_error_item, null);

            ImageView arrowIcon = (ImageView) nextError.findViewById(R.id.arrow_icon);

            TextView header = (TextView) nextError.findViewById(R.id.header);
            header.setText(names.get(i));

            TextView content = (TextView) nextError.findViewById(R.id.content);
            content.setText(errors.get(i));

            if (names.size() == 1) {
                arrowIcon.setVisibility(View.INVISIBLE);
                header.setVisibility(View.INVISIBLE);
                content.setVisibility(View.VISIBLE);
            } else {
                LinearLayout headerContainer = (LinearLayout) nextError.findViewById(R.id.headerContainer);

                final View.OnClickListener toggle = v -> {
                    boolean visible = content.getVisibility() == View.GONE; // make visible now

                    arrowIcon.setImageDrawable(getActivity().getResources().getDrawable(IconDimension.DP_24.getResource(visible)));
                    content.setVisibility(visible ? View.VISIBLE : View.GONE);
                };

                headerContainer.setOnClickListener(toggle);
            }

            container.addView(nextError);
        }

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(names.size() == 1 ? getString(R.string.dialog_one_engine_errors, names.get(0)) : getString(R.string.dialog_engine_errors))
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}
