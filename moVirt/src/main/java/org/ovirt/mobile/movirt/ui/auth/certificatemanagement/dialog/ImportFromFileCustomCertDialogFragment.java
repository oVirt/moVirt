package org.ovirt.mobile.movirt.ui.auth.certificatemanagement.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.dialogs.ListenerDialogFragment;

@EFragment
public class ImportFromFileCustomCertDialogFragment extends ListenerDialogFragment<ImportFromFileCustomCertDialogFragment.ImportIssuerListener> {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.import_from_file_custom_cert_dialog, null);
        ((TextView) view.findViewById(R.id.title)).setText(getString(R.string.cert_dialog_import_issuer));

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(R.string.import_from_file, (dialog, which) -> getListener().onImportIssuer())
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .create();
    }

    public interface ImportIssuerListener {
        void onImportIssuer();
    }
}
