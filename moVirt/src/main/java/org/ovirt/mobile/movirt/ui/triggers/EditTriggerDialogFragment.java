package org.ovirt.mobile.movirt.ui.triggers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.condition.CpuThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.MemoryThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.StatusCondition;

@EFragment
public class EditTriggerDialogFragment extends BaseTriggerDialogFragment {

    private static final String TAG = EditTriggerDialogFragment.class.getSimpleName();

    private Trigger<Vm> trigger;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(getDialogView(R.string.edit_trigger))
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
               })
               .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       deleteTrigger();
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

    public Trigger<Vm> getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger<Vm> trigger) {
        this.trigger = trigger;
    }

    private void saveTrigger() {
        final Condition<Vm> condition = getCondition();
        if (condition == null) {
            return;
        }
        trigger.setCondition(condition);
        trigger.setNotificationType(getNotificationType());

        provider.update(trigger);
    }

    private void deleteTrigger() {
        provider.delete(trigger);
    }
}
