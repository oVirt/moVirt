package org.ovirt.mobile.movirt.ui.triggers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.trigger.Trigger;

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
                       dialog.cancel();
                   }
               });
        return builder.create();
    }

    private void addTrigger() {
        Trigger trigger = new Trigger<>();
        final Condition condition = getCondition();
        if (condition == null) {
            return;
        }
        trigger.setTargetId(triggerActivity.getTargetId());
        trigger.setEntityType(triggerActivity.getEntityType(condition));
        trigger.setCondition(condition);
        trigger.setScope(triggerActivity.getScope());
        trigger.setNotificationType(getNotificationType());

        provider.insert(trigger);
    }
}
