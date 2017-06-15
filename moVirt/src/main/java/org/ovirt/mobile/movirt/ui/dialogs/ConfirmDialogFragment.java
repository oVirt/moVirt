package org.ovirt.mobile.movirt.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;

import org.ovirt.mobile.movirt.R;

/**
 * Dialog asking to confirm destructive action
 * Use newInstance static method to create instance of this dialog
 * Created by Nika on 20.08.2015.
 */
public class ConfirmDialogFragment extends ListenerDialogFragment<ConfirmDialogFragment.ConfirmDialogListener> {

    /**
     * @see ConfirmDialogFragment#newInstance(int, String, String, boolean)
     */
    public static ConfirmDialogFragment newInstance(int actionId, String actionString) {
        return newInstance(actionId, actionString, null, false);
    }

    /**
     * @see ConfirmDialogFragment#newInstance(int, String, String, boolean)
     */
    public static ConfirmDialogFragment newInstance(int actionId, String actionString, boolean customQuestion) {
        return newInstance(actionId, actionString, null, customQuestion);
    }

    /**
     * @see ConfirmDialogFragment#newInstance(int, String, String, boolean)
     */
    public static ConfirmDialogFragment newInstance(int actionId, String actionString, String infoString) {
        return newInstance(actionId, actionString, infoString, false);
    }

    /**
     * Creates instance of ConfirmDialogFragment.
     *
     * @param actionId     - Used to determine which action to perform in listener callback function.
     *                     Can be ignored with 0 if there is only one action in listener.
     * @param actionString - Human-readable string to show in dialog message.
     * @param infoString   - Human-readable string header to show in dialog message.
     * @return - instance of ConfirmDialogFragment.
     * Call .show() on returned value, to display this dialog.
     */
    public static ConfirmDialogFragment newInstance(int actionId, String actionString, String infoString, boolean customQuestion) {
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putInt("actionId", actionId);
        args.putString("actionString", actionString);
        args.putString("infoString", infoString);
        args.putBoolean("customQuestion", customQuestion);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int actionId = getArguments().getInt("actionId");
        String actionString = getArguments().getString("actionString");
        String infoString = getArguments().getString("infoString");
        boolean customQuestion = getArguments().getBoolean("customQuestion");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        StringBuilder message = new StringBuilder();

        if (!TextUtils.isEmpty(infoString)) {
            message.append(infoString).append("\n\n");
        }

        if (customQuestion) {
            message.append(actionString).append('?');
        } else {
            message.append(getString(R.string.dialog_confirm_message, actionString));
        }

        builder.setTitle(android.R.string.dialog_alert_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, (dialog, which) ->
                        getListener().onDialogResult(DialogInterface.BUTTON_POSITIVE, actionId))
                .setNegativeButton(android.R.string.no, (dialog, which) ->
                        getListener().onDialogResult(DialogInterface.BUTTON_NEGATIVE, actionId));
        return builder.create();
    }

    /**
     * Implement this interface in activity that shows ConfirmDialogFragment.
     * switch through actionId to determine action if you have more then one action in activity.
     */
    public interface ConfirmDialogListener {

        void onDialogResult(int dialogButton, int actionId);
    }
}
