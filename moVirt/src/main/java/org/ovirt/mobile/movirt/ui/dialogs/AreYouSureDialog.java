package org.ovirt.mobile.movirt.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;

import org.ovirt.mobile.movirt.R;

public class AreYouSureDialog {
    public static AlertDialog show(Context context, Resources resources, String msg, final Runnable onOk) {
        return new AlertDialog.Builder(context)
                .setMessage(resources.getString(R.string.are_you_sure_str, msg))
                .setPositiveButton(R.string.yes_str, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOk.run();
                    }
                })
                .setNegativeButton(R.string.no_str, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
}
