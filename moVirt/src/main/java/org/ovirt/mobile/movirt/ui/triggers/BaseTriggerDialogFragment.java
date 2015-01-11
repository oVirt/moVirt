package org.ovirt.mobile.movirt.ui.triggers;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.EntityType;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.condition.CpuThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.MemoryThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.StatusCondition;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

@EFragment
public abstract class BaseTriggerDialogFragment extends DialogFragment {
    private Context context;
    TriggerActivity triggerActivity;

    @Bean
    ProviderFacade provider;

    protected Spinner conditionTypeSpinner;
    protected Spinner notificationTypeSpinner;
    protected Spinner statusSpinner;
    protected EditText percentageEdit;

    protected View getDialogView(int titleResourceId) {
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

        context = activity;
        triggerActivity = (TriggerActivity) activity;
    }

    protected Condition<Vm> getCondition() {
        String selectedConditionType = conditionTypeSpinner.getSelectedItem().toString();
        switch (selectedConditionType) {
            case "CPU": {
                if (percentageEdit.getText().length() == 0) {
                    Toast.makeText(getContext(), R.string.percentage_cannot_be_empty, Toast.LENGTH_LONG).show();
                    return null;
                }
                int percentageLimit = asIntWithDefault(percentageEdit.getText().toString(), "0");
                return new CpuThresholdCondition(percentageLimit);
            }
            case "Memory": {
                if (percentageEdit.getText().length() == 0) {
                    Toast.makeText(getContext(), R.string.percentage_cannot_be_empty, Toast.LENGTH_LONG).show();
                    return null;
                }
                int percentageLimit = asIntWithDefault(percentageEdit.getText().toString(), "0");
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

    protected Context getContext() {
        return context;
    }

    private int asIntWithDefault(String toParse, String defaultResult) {
        try {
            return Integer.parseInt(toParse);
        } catch (NumberFormatException e) {
            return Integer.parseInt(defaultResult);
        }
    }
}
