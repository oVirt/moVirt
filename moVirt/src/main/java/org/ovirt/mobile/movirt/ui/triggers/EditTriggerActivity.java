package org.ovirt.mobile.movirt.ui.triggers;

import android.content.DialogInterface;
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
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.condition.CpuThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.EventCondition;
import org.ovirt.mobile.movirt.model.condition.MemoryThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.StatusCondition;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.ui.HasLoader;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;

@EActivity(R.layout.activity_base_trigger)
@OptionsMenu(R.menu.edit_trigger)
public class EditTriggerActivity extends BaseTriggerActivity implements HasLoader,
        LoaderManager.LoaderCallbacks<Cursor>, ConfirmDialogFragment.ConfirmDialogListener {
    private static final String TAG = EditTriggerActivity.class.getSimpleName();

    private static final int TRIGGER_LOADER = 0;
    private static final String TRIGGER_URI = "trigger_uri";

    private static final int DELETE_ACTION = 0;

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

        if (triggerCondition instanceof CpuThresholdCondition) {
            CpuThresholdCondition condition = (CpuThresholdCondition) triggerCondition;
            selectedConditionRadioButton = R.id.radio_button_cpu;
            percentageEdit.setText(Integer.toString(condition.getPercentageLimit()));
        } else if (triggerCondition instanceof MemoryThresholdCondition) {
            MemoryThresholdCondition condition = (MemoryThresholdCondition) triggerCondition;
            selectedConditionRadioButton = R.id.radio_button_memory;
            percentageEdit.setText(Integer.toString(condition.getPercentageLimit()));
        } else if (triggerCondition instanceof StatusCondition) {
            StatusCondition condition = (StatusCondition) triggerCondition;
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

    @OptionsItem(R.id.action_save_trigger)
    public void saveTrigger() {
        final Condition condition = getCondition();
        if (condition == null || trigger == null) {
            return;
        }

        trigger.setCondition(condition);
        trigger.setNotificationType(getNotificationType());

        provider.update(trigger);
        finish();
    }

    public void deleteTrigger() {
        if (trigger != null) {
            provider.delete(trigger);
            finish();
        }
    }

    @OptionsItem(R.id.action_delete_trigger)
    @UiThread
    void delete() {
        ConfirmDialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(DELETE_ACTION, getString(R.string.dialog_action_delete_trigger));
        confirmDialog.show(getFragmentManager(), "confirmDeleteSnapshot");
    }

    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (actionId == DELETE_ACTION && dialogButton == DialogInterface.BUTTON_POSITIVE) {
            deleteTrigger();
        }
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
