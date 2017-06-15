package org.ovirt.mobile.movirt.ui.triggers;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.condition.EventCondition;
import org.ovirt.mobile.movirt.model.condition.VmCpuThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.VmMemoryThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.VmStatusCondition;
import org.ovirt.mobile.movirt.model.mapping.EntityMapper;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.ui.HasLoader;

@EActivity(R.layout.activity_base_trigger)
public class EditTriggerActivity extends BaseTriggerActivity implements HasLoader,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = EditTriggerActivity.class.getSimpleName();

    private static final int TRIGGER_LOADER = 0;
    private static final String TRIGGER_URI = "trigger_uri";

    @InstanceState
    boolean fieldsLoaded = false;

    private Bundle args;
    private Trigger trigger;

    @AfterViews
    public void initLoader() {
        Uri triggerUri = getIntent().getData();

        args = new Bundle();
        args.putParcelable(TRIGGER_URI, triggerUri);
        getSupportLoaderManager().initLoader(TRIGGER_LOADER, args, this);
    }

    @Override
    public void restartLoader() {
        getSupportLoaderManager().restartLoader(TRIGGER_LOADER, args, this);
    }

    @Override
    public void destroyLoader() {
        getSupportLoaderManager().destroyLoader(TRIGGER_LOADER);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = null;

        if (id == TRIGGER_LOADER) {
            String triggerId = args.<Uri>getParcelable(TRIGGER_URI).getLastPathSegment();
            loader = provider.query(Trigger.class).id(triggerId).asLoader();
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToNext()) {
            Log.e(TAG, "Error loading Trigger");
            return;
        }

        if (loader.getId() == TRIGGER_LOADER) {
            trigger = EntityMapper.TRIGGER_MAPPER.fromCursor(data);
            if (!fieldsLoaded) {
                mapExistingTrigger();
                fieldsLoaded = true;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    private void mapExistingTrigger() {
        mapCondition();
        mapNotificationType();
    }

    private void mapCondition() {
        Condition triggerCondition = trigger.getCondition();
        int selectedConditionRadioButton = 0;

        if (triggerCondition instanceof VmCpuThresholdCondition) {
            VmCpuThresholdCondition condition = (VmCpuThresholdCondition) triggerCondition;
            selectedConditionRadioButton = R.id.radio_button_cpu;
            percentageEdit.setText(Integer.toString(condition.getPercentageLimit()));
        } else if (triggerCondition instanceof VmMemoryThresholdCondition) {
            VmMemoryThresholdCondition condition = (VmMemoryThresholdCondition) triggerCondition;
            selectedConditionRadioButton = R.id.radio_button_memory;
            percentageEdit.setText(Integer.toString(condition.getPercentageLimit()));
        } else if (triggerCondition instanceof VmStatusCondition) {
            VmStatusCondition condition = (VmStatusCondition) triggerCondition;
            selectedConditionRadioButton = R.id.radio_button_status;
            int index = ((ArrayAdapter<String>) statusSpinner.getAdapter()).getPosition(condition.getStatus().toString().toUpperCase());
            statusSpinner.setSelection(index);
        } else if (triggerCondition instanceof EventCondition) {
            EventCondition condition = (EventCondition) triggerCondition;
            selectedConditionRadioButton = R.id.radio_button_event;
            regexEdit.setText(condition.getRegexString());
        }

        conditionRadioGroup.check(selectedConditionRadioButton);
        onRadioButtonClicked(selectedConditionRadioButton);
    }

    private void mapNotificationType() {
        switch (trigger.getNotificationType()) {
            case INFO:
                notificationRadioGroup.check(R.id.radio_button_blink);
                break;
            case CRITICAL:
                notificationRadioGroup.check(R.id.radio_button_vibrate);
                break;
        }
    }

    @Override
    public void onDone() {
        final Condition condition = getCondition();
        if (condition == null || trigger == null) {
            return;
        }

        trigger.setCondition(condition);
        trigger.setNotificationType(getNotificationType());

        provider.update(trigger);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restartLoader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyLoader();
    }
}
