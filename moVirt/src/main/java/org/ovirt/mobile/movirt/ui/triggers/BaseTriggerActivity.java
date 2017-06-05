package org.ovirt.mobile.movirt.ui.triggers;

import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.data.Selection;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.condition.EventCondition;
import org.ovirt.mobile.movirt.model.condition.VmCpuThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.VmMemoryThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.VmStatusCondition;
import org.ovirt.mobile.movirt.model.enums.VmStatus;
import org.ovirt.mobile.movirt.model.mapping.EntityType;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.BroadcastAwareAppCompatActivity;
import org.ovirt.mobile.movirt.util.resources.Resources;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity.EXTRA_SELECTION;
import static org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity.EXTRA_TARGET_ENTITY_ID;

@EActivity(R.layout.activity_base_trigger)
public abstract class BaseTriggerActivity extends BroadcastAwareAppCompatActivity {

    private String targetEntityId;

    @InstanceState
    protected int selectedCondition = R.id.radio_button_cpu;

    @InstanceState
    protected Selection selection;

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

    @ViewById
    FloatingActionButton fab;

    @Bean
    ProviderFacade provider;

    @Bean
    Resources resources;

    @ViewById
    TextView statusText;

    @AfterViews
    void init() {
        onRadioButtonClicked(selectedCondition); // for screen rotation//

        targetEntityId = getIntent().getStringExtra(EXTRA_TARGET_ENTITY_ID);
        selection = getIntent().getParcelableExtra(EXTRA_SELECTION);

        statusText.setText(selection.getDescription());

        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_green_300)));
        fab.setOnClickListener(view -> onDone());
    }

    protected abstract void onDone();

    public String getTargetEntityId() {
        return targetEntityId;
    }

    public Selection getSelection() {
        return selection;
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
                return new VmCpuThresholdCondition(percentageLimit);
            }
            case R.id.radio_button_memory: {
                if (percentageEdit.getText().length() == 0) {
                    Toast.makeText(this, R.string.percentage_cannot_be_empty, Toast.LENGTH_LONG).show();
                    return null;
                }
                int percentageLimit = asIntWithDefault(percentageEdit.getText().toString(), "0");
                return new VmMemoryThresholdCondition(percentageLimit);
            }
            case R.id.radio_button_status: {
                VmStatus status = VmStatus.fromString(statusSpinner.getSelectedItem().toString());
                return new VmStatusCondition(status);
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
}
