package org.ovirt.mobile.movirt.ui.triggers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.ArrayAdapter;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.condition.CpuThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.MemoryThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.StatusCondition;
import org.ovirt.mobile.movirt.provider.OVirtContract;

public class EditTriggerDialogFragment extends BaseTriggerDialogFragment {

    private final Trigger<Vm> trigger;

    @SuppressWarnings("unchecked")
    public EditTriggerDialogFragment(Cursor cursor) {
        super(R.string.edit_trigger);

        this.trigger = (Trigger<Vm>) EntityMapper.TRIGGER_MAPPER.fromCursor(cursor);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(getDialogView())
               .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       saveTrigger();
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       getDialog().cancel();
                   }
               });
        mapExistingTrigger();
        return builder.create();
    }

    private void mapExistingTrigger() {
        mapCondition();
        mapNotificationType();
    }

    private void mapCondition() {
        if (trigger.getCondition() instanceof CpuThresholdCondition) {
            CpuThresholdCondition condition = (CpuThresholdCondition) trigger.getCondition();
            conditionTypeSpinner.setSelection(0);
            percentageEdit.setText(Integer.toString(condition.percentageLimit));
        } else if (trigger.getCondition() instanceof MemoryThresholdCondition) {
            MemoryThresholdCondition condition = (MemoryThresholdCondition) trigger.getCondition();
            conditionTypeSpinner.setSelection(1);
            percentageEdit.setText(Integer.toString(condition.percentageLimit));
        } else if (trigger.getCondition() instanceof StatusCondition) {
            StatusCondition condition = (StatusCondition) trigger.getCondition();
            conditionTypeSpinner.setSelection(2);
            int index = ((ArrayAdapter<String>) statusSpinner.getAdapter()).getPosition(condition.status.toString().toUpperCase());
            statusSpinner.setSelection(index);
        }
    }

    private void mapNotificationType() {
        switch (trigger.getNotificationType()) {
            case INFO:
                notificationTypeSpinner.setSelection(0);
                break;
            case CRITICAL:
                notificationTypeSpinner.setSelection(1);
                break;
        }
    }

    private void saveTrigger() {
        final Condition<Vm> condition = getCondition();
        if (condition == null) {
            return;
        }
        trigger.setCondition(condition);
        trigger.setNotificationType(getNotificationType());
        try {
            client.update(OVirtContract.Trigger.CONTENT_URI, trigger.toValues(),
                          OVirtContract.Trigger._ID + " = ?", new String[]{Integer.toString(trigger.getId())});
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
