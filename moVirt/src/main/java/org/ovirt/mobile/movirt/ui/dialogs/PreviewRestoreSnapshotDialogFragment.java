package org.ovirt.mobile.movirt.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.BooleanListener;

@EFragment
public class PreviewRestoreSnapshotDialogFragment extends DialogFragment {

    private BooleanListener listenerActivity;

    public static PreviewRestoreSnapshotDialogFragment newInstance(int actionId, String actionString) {
        PreviewRestoreSnapshotDialogFragment fragment = new PreviewRestoreSnapshotDialogFragment_();
        Bundle args = new Bundle();
        args.putInt("actionId", actionId);
        args.putString("actionString", actionString);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listenerActivity = (BooleanListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NewSnapshotListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int actionId = getArguments().getInt("actionId");
        String actionString = getArguments().getString("actionString");
        String actionHeader = String.format("%s %s", actionString, getString(R.string.snapshot));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.preview_restore_snapshot_dialog, null);
        final CheckBox restoreMemory = (CheckBox) view.findViewById(R.id.restore_memory);
        final TextView textView = (TextView) view.findViewById(R.id.headText);
        textView.setText(actionHeader);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(actionString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listenerActivity.onDialogResult(actionId, restoreMemory.isChecked());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }
}
