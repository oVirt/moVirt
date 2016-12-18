package org.ovirt.mobile.movirt.ui.triggers;

import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.mapping.EntityType;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.condition.CpuThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.EventCondition;
import org.ovirt.mobile.movirt.model.condition.MemoryThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.StatusCondition;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiver;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiverHelper;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity.EXTRA_SCOPE;
import static org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity.EXTRA_TARGET_ENTITY_ID;

@EActivity(R.layout.activity_base_trigger)
public abstract class BaseTriggerActivity extends ActionBarActivity implements CreateDialogBroadcastReceiver {

    @InstanceState
    protected int selectedCondition = R.id.radio_button_cpu;

    @ViewById(R.id.rangePanel)
    LinearLayout rangePanel;

    @ViewById(R.id.statusPanel)
    LinearLayout statusPanel;

    @ViewById(R.id.regexPanel)
    LinearLayout regexPanel;

    @ViewById(R.id.conditionRadioGroup)
    RadioGroup conditionRadioGroup;

    @ViewById(R.id.notificationRadioGroup)
    RadioGroup notificationRadioGroup;

    @ViewById(R.id.statusSpinner)
    Spinner statusSpinner;

    @ViewById(R.id.percentageEdit)
    EditText percentageEdit;

    @ViewById(R.id.regexEdit)
    EditText regexEdit;

    @Bean
    ProviderFacade provider;

    private String targetEntityId;
    private Trigger.Scope triggerScope;

    @AfterViews
    void init() {
        onRadioButtonClicked(selectedCondition); // for screen rotation

        targetEntityId = getIntent().getStringExtra(EXTRA_TARGET_ENTITY_ID);
        triggerScope = (Trigger.Scope) getIntent().getSerializableExtra(EXTRA_SCOPE);
    }

    public String getTargetEntityId() {
        return targetEntityId;
    }

    public Trigger.Scope getTriggerScope() {
        return triggerScope;
    }

    public void onRadioButtonClicked(View view) {
        onRadioButtonClicked(view.getId());
    }

    public void onRadioButtonClicked(int id) {
        switch (id) {
            case R.id.radio_button_cpu:
            case R.id.radio_button_memory:
                rangePanel.setVisibility(View.VISIBLE);
                statusPanel.setVisibility(View.GONE);
                regexPanel.setVisibility(View.GONE);
                break;
            case R.id.radio_button_status:
                rangePanel.setVisibility(View.GONE);
                statusPanel.setVisibility(View.VISIBLE);
                regexPanel.setVisibility(View.GONE);
                break;
            case R.id.radio_button_event:
                rangePanel.setVisibility(View.GONE);
                statusPanel.setVisibility(View.GONE);
                regexPanel.setVisibility(View.VISIBLE);
                break;
        }
        selectedCondition = id;
    }

    protected Condition getCondition() {
        switch (selectedCondition) {
            case R.id.radio_button_cpu: {
                if (percentageEdit.getText().length() == 0) {
                    Toast.makeText(this, R.string.percentage_cannot_be_empty, Toast.LENGTH_LONG).show();
                    return null;
                }
                int percentageLimit = asIntWithDefault(percentageEdit.getText().toString(), "0");
                return new CpuThresholdCondition(percentageLimit);
            }
            case R.id.radio_button_memory: {
                if (percentageEdit.getText().length() == 0) {
                    Toast.makeText(this, R.string.percentage_cannot_be_empty, Toast.LENGTH_LONG).show();
                    return null;
                }
                int percentageLimit = asIntWithDefault(percentageEdit.getText().toString(), "0");
                return new MemoryThresholdCondition(percentageLimit);
            }
            case R.id.radio_button_status: {
                Vm.Status status = Vm.Status.valueOf(statusSpinner.getSelectedItem().toString().toUpperCase());
                return new StatusCondition(status);
            }
            case R.id.radio_button_event: {
                //do not allow empty regex string
                if (regexEdit.getText().length() == 0) {
                    Toast.makeText(this, R.string.regex_cannot_be_empty, Toast.LENGTH_LONG).show();
                    return null;
                }
                //check regex syntax and create Pattern from String
                Pattern pattern;
                try {
                    pattern = Pattern.compile(regexEdit.getText().toString());
                } catch (PatternSyntaxException pse) {
                    Toast.makeText(this, R.string.regex_syntax_error, Toast.LENGTH_LONG).show();
                    return null;
                }
                return new EventCondition(pattern);
            }
            default:
                throw new RuntimeException("Unknown condition type selected");
        }
    }

    public EntityType getEntityType(Condition triggerCondition) {
        if (triggerCondition instanceof EventCondition) {
            return EntityType.EVENT;
        } // add else if when more Entity types will be added to Add Trigger menu
        return EntityType.VM;
    }

    protected Trigger.NotificationType getNotificationType() {
        return notificationRadioGroup.getCheckedRadioButtonId() == R.id.radio_button_blink ?
                Trigger.NotificationType.INFO :
                Trigger.NotificationType.CRITICAL;
    }

    private int asIntWithDefault(String toParse, String defaultResult) {
        try {
            return Integer.parseInt(toParse);
        } catch (NumberFormatException e) {
            return Integer.parseInt(defaultResult);
        }
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        onBackPressed();
    }

    @Receiver(actions = {Broadcasts.ERROR_MESSAGE},
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    public void showErrorDialog(
            @Receiver.Extra(Broadcasts.Extras.ERROR_REASON) String reason,
            @Receiver.Extra(Broadcasts.Extras.REPEATED_MINOR_ERROR) boolean repeatedMinorError) {
        CreateDialogBroadcastReceiverHelper.showErrorDialog(getFragmentManager(), reason, repeatedMinorError);
    }

    @Receiver(actions = {Broadcasts.REST_CA_FAILURE},
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    public void showCertificateDialog(
            @Receiver.Extra(Broadcasts.Extras.ERROR_REASON) String reason) {
        CreateDialogBroadcastReceiverHelper.showCertificateDialog(getFragmentManager(), reason, true);
    }
}
