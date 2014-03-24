package org.ovirt.mobile.movirt.ui.triggers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.provider.OVirtContract;

public class AddTriggerDialogFragment extends BaseTriggerDialogFragment {
    private static final String TAG = AddTriggerDialogFragment.class.getSimpleName();

    protected AddTriggerDialogFragment() {
        super(R.string.add_trigger);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getDialogView();
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
        try {
            client.insert(OVirtContract.Trigger.CONTENT_URI, trigger.toValues());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
