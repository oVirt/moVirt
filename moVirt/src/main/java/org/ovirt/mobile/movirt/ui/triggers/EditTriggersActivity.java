package org.ovirt.mobile.movirt.ui.triggers;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import org.androidannotations.annotations.ItemClick;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.EntityType;
import org.ovirt.mobile.movirt.model.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.condition.CpuThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.MemoryThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.StatusCondition;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_edit_triggers)
public class EditTriggersActivity extends Activity implements BaseTriggerDialogFragment.TriggerActivity {
    public static final String EXTRA_TARGET_ENTITY_ID = "target_entity";
    public static final String EXTRA_TARGET_ENTITY_NAME = "target_name";
    public static final String EXTRA_SCOPE = "scope";

    private static final String[] PROJECTION = new String[] {
            OVirtContract.Trigger.ID,
            OVirtContract.Trigger.CONDITION,
            OVirtContract.Trigger.NOTIFICATION,
            OVirtContract.Trigger.SCOPE,
            OVirtContract.Trigger.TARGET_ID,
            OVirtContract.Trigger.ENTITY_TYPE,
    };

    private String targetEntityId;
    private String targetEntityName;

    private Trigger.Scope triggerScope;

    private SimpleCursorAdapter triggerAdapter;
    private CursorAdapterLoader cursorAdapterLoader;

    @StringRes(R.string.whole_datacenter)
    String GLOBAL_SCOPE;

    @StringRes(R.string.cluster_scope)
    String CLUSTER_SCOPE;

    @StringRes(R.string.vm_scope)
    String ITEM_SCOPE;

    @AfterViews
    void init() {
        targetEntityId = getIntent().getStringExtra(EXTRA_TARGET_ENTITY_ID);
        targetEntityName = getIntent().getStringExtra(EXTRA_TARGET_ENTITY_NAME);
        triggerScope = (Trigger.Scope) getIntent().getSerializableExtra(EXTRA_SCOPE);

        triggerScopeLabel.setText(getTitleText());

        triggerAdapter = new SimpleCursorAdapter(this,
                                                 R.layout.trigger_item,
                                                 null,
                                                 PROJECTION,
                                                 new int[] {R.id.trigger_id});
        triggerAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                TextView textView = (TextView) view;
                textView.setText(getTriggerString((Trigger<Vm>) EntityMapper.TRIGGER_MAPPER.fromCursor(cursor)));
                return true;
            }
        });

        cursorAdapterLoader = new CursorAdapterLoader(triggerAdapter) {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(EditTriggersActivity.this,
                                        OVirtContract.Trigger.CONTENT_URI,
                                        PROJECTION,
                                        getTriggerSelection(),
                                        getTriggerSelectionArgs(),
                                        null);
            }
        };

        triggersListView.setAdapter(triggerAdapter);
        triggersListView.setEmptyView(findViewById(android.R.id.empty));

        getLoaderManager().initLoader(0, null, cursorAdapterLoader);
    }

    private String getTitleText() {
        switch (triggerScope) {
            case GLOBAL:
                return GLOBAL_SCOPE;
            case CLUSTER:
                return String.format(CLUSTER_SCOPE, targetEntityName);
            case ITEM:
                return String.format(ITEM_SCOPE, targetEntityName);
        }
        return "unexpected title";
    }

    private String getTriggerString(Trigger<Vm> trigger) {
        StringBuilder builder =  new StringBuilder()
                .append(trigger.getNotificationType() == Trigger.NotificationType.INFO ? "Blink" : "Vibrate")
                .append(" when ");
        if (trigger.getCondition() instanceof CpuThresholdCondition) {
            CpuThresholdCondition condition = (CpuThresholdCondition) trigger.getCondition();
            builder.append("CPU above ").append(condition.percentageLimit).append("%");
        } else if (trigger.getCondition() instanceof MemoryThresholdCondition) {
            MemoryThresholdCondition condition = (MemoryThresholdCondition) trigger.getCondition();
            builder.append("Memory above ").append(condition.percentageLimit).append("%");
        } else if (trigger.getCondition() instanceof StatusCondition) {
            StatusCondition condition = (StatusCondition) trigger.getCondition();
            builder.append("Status is ").append(condition.status.toString());
        }
        return builder.toString();
    }

    @ViewById
    TextView triggerScopeLabel;

    @ViewById
    ListView triggersListView;

    @Click
    void addTrigger() {
        AddTriggerDialogFragment dialog = new AddTriggerDialogFragment_();
        dialog.show(getFragmentManager(), "");
    }

    @ItemClick
    void triggersListViewItemClicked(Cursor cursor) {
        EditTriggerDialogFragment dialog = new EditTriggerDialogFragment_();
        dialog.setTrigger((Trigger<Vm>) EntityMapper.TRIGGER_MAPPER.fromCursor(cursor));
        dialog.show(getFragmentManager(), "");
    }

    private String getTriggerSelection() {
        return OVirtContract.Trigger.ENTITY_TYPE + " = ? AND " +
               OVirtContract.Trigger.SCOPE + " = ? AND " +
               OVirtContract.Trigger.TARGET_ID + (getTargetId() == null ? " IS NULL" : " = ?");
    }

    private String[] getTriggerSelectionArgs() {
        List<String> args = new ArrayList<>();
        args.add(getEntityType().toString());
        args.add(getScope().toString());
        if (getTargetId() != null) {
            args.add(getTargetId());
        }
        return args.toArray(new String[args.size()]);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.VM;
    }

    @Override
    public Trigger.Scope getScope() {
        return triggerScope;
    }

    @Override
    public String getTargetId() {
        return targetEntityId;
    }
}
