package org.ovirt.mobile.movirt.ui.triggers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.condition.Condition;

@EFragment
public class AddTriggerDialogFragment extends BaseTriggerDialogFragment {
    private static final String TAG = AddTriggerDialogFragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getDialogView(R.string.add_trigger);
        builder.setView(view)
               .setPositiveButton(R.string.add_trigger, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       addTrigger();
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       getDialog().cancel();
                   }
               });
        return builder.create();
    }

    private void addTrigger() {
        Trigger<Vm> trigger = new Trigger<>();
        trigger.setTargetId(triggerActivity.getTargetId());
        trigger.setEntityType(triggerActivity.getEntityType());
        final Condition<Vm> condition = getCondition();
        if (condition == null) {
            return;
        }
        trigger.setCondition(condition);
        trigger.setScope(triggerActivity.getScope());
        trigger.setNotificationType(getNotificationType());

        provider.insert(trigger);
    }
}
