package org.ovirt.mobile.movirt.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
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
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.enums.VmCommand;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.dto.Snapshot;
import org.springframework.util.StringUtils;

@EFragment
public class CreateSnapshotDialogFragment extends ListenerDialogFragment<DialogListener.NewSnapshotListener> {

    @InstanceState
    protected String vmId;

    @InstanceState
    protected MovirtAccount account;

    @Bean
    protected EnvironmentStore environmentStore;

    private Vm currentVm;

    private CheckBox persistMemory;
    private EditText descriptionEdit;

    @Bean
    ProviderFacade providerFacade;

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    public void setAccount(MovirtAccount account) {
        this.account = account;
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

        if (VmCommand.SAVE_MEMORY.canExecute(currentVm.getStatus())) {
            saveMemoryLayout.setVisibility(View.VISIBLE);
            persistMemory.setChecked(true);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.create, (dialog, which) -> {
            String description = descriptionEdit.getText().toString();
            boolean persistMem = persistMemory.isChecked();
            Snapshot snapshot = environmentStore.getVersion(account).isV3Api() ?
                    new org.ovirt.mobile.movirt.rest.dto.v3.Snapshot(description, persistMem) :
                    new org.ovirt.mobile.movirt.rest.dto.v4.Snapshot(description, persistMem);
            getListener().onDialogResult(snapshot);
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            final Button button = ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE);

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
        });

        return dialog;
    }
}
