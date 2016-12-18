package org.ovirt.mobile.movirt.ui.triggers;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.mapping.EntityMapper;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.condition.CpuThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.EventCondition;
import org.ovirt.mobile.movirt.model.condition.MemoryThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.StatusCondition;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.ActionBarLoaderActivity;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Trigger.SCOPE;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Trigger.TARGET_ID;

@EActivity(R.layout.activity_edit_triggers)
@OptionsMenu(R.menu.triggers)
public class EditTriggersActivity extends ActionBarLoaderActivity {
    public static final String EXTRA_TARGET_ENTITY_ID = "target_entity";
    public static final String EXTRA_TARGET_ENTITY_NAME = "target_name";
    public static final String EXTRA_SCOPE = "scope";

    private static final String[] PROJECTION = new String[]{
            OVirtContract.Trigger.CONDITION,
            OVirtContract.Trigger.NOTIFICATION,
    };

    private String targetEntityId;
    private String targetEntityName;

    private Trigger.Scope triggerScope;
    private CursorAdapterLoader cursorAdapterLoader;

    @Bean
    ProviderFacade provider;

    @StringRes(R.string.whole_datacenter)
    String GLOBAL_SCOPE;

    @StringRes(R.string.cluster_scope)
    String CLUSTER_SCOPE;

    @StringRes(R.string.vm_scope)
    String ITEM_SCOPE;

    @StringRes(R.string.trigger_title_format)
    String TITLE_FORMAT;

    @AfterViews
    void init() {
        targetEntityId = getIntent().getStringExtra(EXTRA_TARGET_ENTITY_ID);
        targetEntityName = getIntent().getStringExtra(EXTRA_TARGET_ENTITY_NAME);
        triggerScope = (Trigger.Scope) getIntent().getSerializableExtra(EXTRA_SCOPE);

        setTitle(String.format(TITLE_FORMAT, getScopeText()));

        SimpleCursorAdapter triggerAdapter = new SimpleCursorAdapter(this,
                R.layout.trigger_item,
                null,
                PROJECTION,
                new int[]{R.id.trigger_condition, R.id.trigger_notification}, 0);
        triggerAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                TextView textView = (TextView) view;
                Trigger trigger = (Trigger) EntityMapper.TRIGGER_MAPPER.fromCursor(cursor);
                if (columnIndex == cursor.getColumnIndex(OVirtContract.Trigger.NOTIFICATION)) {
                    textView.setText(trigger.getNotificationType().getDisplayResourceId());
                } else if (columnIndex == cursor.getColumnIndex(OVirtContract.Trigger.CONDITION)) {
                    textView.setText(getConditionString(trigger.getCondition()));
                }
                return true;
            }
        });

        cursorAdapterLoader = new CursorAdapterLoader(triggerAdapter) {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return provider
                        .query(Trigger.class)
                        //.where(ENTITY_TYPE, getEntityType().toString()) //do not filter trigger by entity type
                        .where(SCOPE, triggerScope.toString())
                        .where(TARGET_ID, targetEntityId)
                        .asLoader();
            }
        };

        triggersListView.setAdapter(triggerAdapter);
        triggersListView.setEmptyView(findViewById(android.R.id.empty));

        getSupportLoaderManager().initLoader(0, null, cursorAdapterLoader);
    }

    @Override
    public void restartLoader() {
        getSupportLoaderManager().restartLoader(0, null, cursorAdapterLoader);
    }

    @Override
    public void destroyLoader() {
        getSupportLoaderManager().destroyLoader(0);
    }

    private String getScopeText() {
        switch (triggerScope) {
            case GLOBAL:
                return GLOBAL_SCOPE;
            case CLUSTER:
                return String.format(CLUSTER_SCOPE, targetEntityName);
            case ITEM:
                return String.format(ITEM_SCOPE, targetEntityName);
        }
        return "unexpected scope";
    }

    private String getConditionString(Condition triggerCondition) {
        StringBuilder builder = new StringBuilder();
        if (triggerCondition instanceof CpuThresholdCondition) {
            CpuThresholdCondition condition = (CpuThresholdCondition) triggerCondition;
            builder.append("CPU above ").append(condition.getPercentageLimit()).append("%");
        } else if (triggerCondition instanceof MemoryThresholdCondition) {
            MemoryThresholdCondition condition = (MemoryThresholdCondition) triggerCondition;
            builder.append("Memory above ").append(condition.getPercentageLimit()).append("%");
        } else if (triggerCondition instanceof StatusCondition) {
            StatusCondition condition = (StatusCondition) triggerCondition;
            builder.append("Status is ").append(condition.getStatus().toString());
        } else if (triggerCondition instanceof EventCondition) {
            EventCondition condition = (EventCondition) triggerCondition;
            builder.append("Event matches ").append(condition.getRegexString());
        }
        return builder.toString();
    }

    @ViewById
    ListView triggersListView;

    @OptionsItem(R.id.action_add_trigger)
    void addTrigger() {
        Intent intent = getTriggerActivityIntent(AddTriggerActivity_.class);
        startActivity(intent);
    }

    @ItemClick
    void triggersListViewItemClicked(Cursor cursor) {
        Trigger trigger = (Trigger) EntityMapper.TRIGGER_MAPPER.fromCursor(cursor);
        Intent intent = getTriggerActivityIntent(EditTriggerActivity_.class, trigger.getUri());
        startActivity(intent);
    }

    private Intent getTriggerActivityIntent(Class<?> clazz) {
        return getTriggerActivityIntent(clazz, null);
    }

    private Intent getTriggerActivityIntent(Class<?> clazz, Uri uri) {
        Intent intent = new Intent(getApplicationContext(), clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtras(getIntent());
        if (uri != null) {
            intent.setData(uri);
        }

        return intent;
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        onBackPressed(); // home behaves like back button - we need to return to vm from triggers
    }
}
