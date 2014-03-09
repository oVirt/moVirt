package org.ovirt.mobile.movirt.ui.triggers;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentProviderClient;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.EntityType;
import org.ovirt.mobile.movirt.model.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.condition.CpuThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.MemoryThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.StatusCondition;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.w3c.dom.Text;

public abstract class BaseTriggerDialogFragment extends DialogFragment {
    private final int titleResourceId;
    ContentProviderClient client;
    TriggerActivity triggerActivity;

    protected Spinner conditionTypeSpinner;
    protected Spinner notificationTypeSpinner;
    protected Spinner statusSpinner;
    protected EditText percentageEdit;

    protected BaseTriggerDialogFragment(int titleResourceId) {
        this.titleResourceId = titleResourceId;
    }

    protected View getDialogView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.trigger_dialog, null);
        final ViewGroup rangePanel = (ViewGroup) view.findViewById(R.id.rangePanel);
        final ViewGroup statusPanel = (ViewGroup) view.findViewById(R.id.statusPanel);

        final TextView headText = (TextView) view.findViewById(R.id.headText);
        headText.setText(titleResourceId);

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

    interface TriggerActivity {
        EntityType getEntityType();

        Trigger.Scope getScope();

        String getTargetId();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        client = activity.getContentResolver().acquireContentProviderClient(OVirtContract.BASE_CONTENT_URI);
        triggerActivity = (TriggerActivity) activity;
    }

    protected Condition<Vm> getCondition() {
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

    protected Trigger.NotificationType getNotificationType() {
        return notificationTypeSpinner.getSelectedItem().equals("Blink") ?
                Trigger.NotificationType.INFO :
                Trigger.NotificationType.CRITICAL;
    }
}
