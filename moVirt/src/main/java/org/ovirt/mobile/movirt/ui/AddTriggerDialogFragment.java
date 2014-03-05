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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.condition.CpuThresholdCondition;
import org.ovirt.mobile.movirt.model.EntityType;
import org.ovirt.mobile.movirt.model.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.condition.MemoryThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.StatusCondition;
import org.ovirt.mobile.movirt.provider.OVirtContract;

public class AddTriggerDialogFragment extends DialogFragment {
    private static final String TAG = AddTriggerActivity.class.getSimpleName();
    ContentProviderClient client;
    AddTriggerActivity addTriggerActivity;

    Spinner conditionTypeSpinner;
    Spinner notificationTypeSpinner;
    Spinner statusSpinner;
    EditText percentageEdit;

    interface AddTriggerActivity {
        EntityType getEntityType();

        Trigger.Scope getScope();

        String getTargetId();
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

    private View getDialogView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.add_trigger_dialog, null);
        final ViewGroup rangePanel = (ViewGroup) view.findViewById(R.id.rangePanel);
        final ViewGroup statusPanel = (ViewGroup) view.findViewById(R.id.statusPanel);

        conditionTypeSpinner = (Spinner) view.findViewById(R.id.conditionType);
        notificationTypeSpinner = (Spinner) view.findViewById(R.id.notificationSpinner);
        statusSpinner = (Spinner) view.findViewById(R.id.statusSpinner);
        percentageEdit = (EditText) view.findViewById(R.id.percentageEdit);

        conditionTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedConditionType = parent.getItemAtPosition(position).toString();
                switch (selectedConditionType) {
                    case "CPU":
                    case "Memory":
                        rangePanel.setVisibility(View.VISIBLE);
                        statusPanel.setVisibility(View.GONE);
                        break;
                    case "Status":
                        rangePanel.setVisibility(View.GONE);
                        statusPanel.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                rangePanel.setVisibility(View.GONE);
                statusPanel.setVisibility(View.GONE);
            }
        });

        return view;
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
        trigger.setCondition(getCondition());
        trigger.setScope(addTriggerActivity.getScope());
        trigger.setNotificationType(getNotificationType());
        try {
            client.insert(OVirtContract.Trigger.CONTENT_URI, trigger.toValues());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public Condition<Vm> getCondition() {
        String selectedConditionType = conditionTypeSpinner.getSelectedItem().toString();
        switch (selectedConditionType) {
            case "CPU": {
                int percentageLimit = Integer.parseInt(percentageEdit.getText().toString());
                return new CpuThresholdCondition(percentageLimit);
            }
            case "Memory": {
                int percentageLimit = Integer.parseInt(percentageEdit.getText().toString());
                return new MemoryThresholdCondition(percentageLimit);
            }
            case "Status": {
                Vm.Status status = Vm.Status.valueOf(statusSpinner.getSelectedItem().toString().toUpperCase());
                return new StatusCondition(status);
            }
            default:
                throw new RuntimeException("Unknown condition type selected");
        }
    }

    public Trigger.NotificationType getNotificationType() {
        return notificationTypeSpinner.getSelectedItem().equals("Blink") ?
                Trigger.NotificationType.INFO :
                Trigger.NotificationType.CRITICAL;
    }
}
