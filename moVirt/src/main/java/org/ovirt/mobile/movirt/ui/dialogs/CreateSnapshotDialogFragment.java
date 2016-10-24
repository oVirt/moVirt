package org.ovirt.mobile.movirt.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.NewSnapshotListener;
import org.springframework.util.StringUtils;

/**
 * Created by suomiy on 15/02/16.
 */

@EFragment
public class CreateSnapshotDialogFragment extends DialogFragment {

    @Bean
    ProviderFacade providerFacade;

    @Bean
    MovirtAuthenticator authenticator;

    private NewSnapshotListener listenerActivity;

    private String vmId;
    private Vm currentVm;

    private CheckBox persistMemory;
    private EditText descriptionEdit;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listenerActivity = (NewSnapshotListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NewSnapshotListener");
        }
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    @AfterInject
    public void setVm() {
        if (!StringUtils.isEmpty(vmId)) {
            currentVm = providerFacade.query(Vm.class).id(vmId).first();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.create_snapshot_dialog, null);
        LinearLayout saveMemoryLayout = (LinearLayout) view.findViewById(R.id.save_memory_layout);

        persistMemory = (CheckBox) view.findViewById(R.id.persist_memory);
        descriptionEdit = (EditText) view.findViewById(R.id.description_edit);

        if (Vm.Command.SAVE_MEMORY.canExecute(currentVm.getStatus())) {
            saveMemoryLayout.setVisibility(View.VISIBLE);
            persistMemory.setChecked(true);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String description = descriptionEdit.getText().toString();
                boolean persistMem = persistMemory.isChecked();

                org.ovirt.mobile.movirt.rest.Snapshot snapshot = authenticator.isV3Api() ?
                        new org.ovirt.mobile.movirt.rest.v3.Snapshot(description, persistMem) :
                        new org.ovirt.mobile.movirt.rest.v4.Snapshot(description, persistMem);
                listenerActivity.onDialogResult(snapshot);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                if (button != null) {
                    button.setEnabled(false);
                    descriptionEdit.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            button.setEnabled(s.length() != 0);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });
                }
            }
        });

        return dialog;
    }
}
