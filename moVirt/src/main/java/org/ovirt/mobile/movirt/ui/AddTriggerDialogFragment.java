package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentProviderClient;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.DummyVmCondition;
import org.ovirt.mobile.movirt.model.EntityType;
import org.ovirt.mobile.movirt.model.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;

public class AddTriggerDialogFragment extends DialogFragment {
    ContentProviderClient client;
    AddTriggerActivity addTriggerActivity;

    interface AddTriggerActivity {
        EntityType getEntityType();

        Trigger.Scope getScope();

        String getTargetId();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.add_trigger_dialog, null))
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        client = activity.getContentResolver().acquireContentProviderClient(OVirtContract.BASE_CONTENT_URI);
        addTriggerActivity = (AddTriggerActivity) activity;
    }

    private void addTrigger() {
        Trigger<Vm> trigger = new Trigger<>();
        trigger.setTargetId(addTriggerActivity.getTargetId());
        trigger.setEntityType(addTriggerActivity.getEntityType());
        trigger.setCondition(new DummyVmCondition());
        trigger.setScope(addTriggerActivity.getScope());
        trigger.setNotificationType(Trigger.NotificationType.INFO);
        try {
            client.insert(OVirtContract.Trigger.CONTENT_URI, trigger.toValues());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
