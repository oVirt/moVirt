package org.ovirt.mobile.movirt.ui.triggers;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.condition.CpuThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.EventCondition;
import org.ovirt.mobile.movirt.model.condition.MemoryThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.StatusCondition;
import org.ovirt.mobile.movirt.model.mapping.EntityMapper;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.ActionBarLoaderActivity;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Trigger.SCOPE;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Trigger.TARGET_ID;

@EActivity(R.layout.activity_edit_triggers)
@OptionsMenu(R.menu.delete_item)
public class EditTriggersActivity extends ActionBarLoaderActivity implements ConfirmDialogFragment.ConfirmDialogListener {
    public static final String EXTRA_TARGET_ENTITY_ID = "target_entity";
    public static final String EXTRA_TARGET_ENTITY_NAME = "target_name";
    public static final String EXTRA_SCOPE = "scope";

    private static final String[] PROJECTION = new String[]{
            OVirtContract.Trigger.CONDITION,
            OVirtContract.Trigger.NOTIFICATION,
    };
    private static final int DELETE_ACTION = 0;

    private String targetEntityId;
    private String targetEntityName;

    private Trigger.Scope triggerScope;
    private CursorAdapterLoader cursorAdapterLoader;

    @Bean
    ProviderFacade provider;

    @ViewById
    FloatingActionButton fab;

    @StringRes(R.string.whole_datacenter)
    String GLOBAL_SCOPE;

    @StringRes(R.string.cluster_scope)
    String CLUSTER_SCOPE;

    @StringRes(R.string.vm_scope)
    String ITEM_SCOPE;

    @StringRes(R.string.trigger_title_format)
    String TITLE_FORMAT;

    @OptionsMenuItem
    MenuItem deleteItem;

    @ViewById
    ListView listView;

    @InstanceState
    Integer selectedListItem;

    @AfterViews
    void init() {
        targetEntityId = getIntent().getStringExtra(EXTRA_TARGET_ENTITY_ID);
        targetEntityName = getIntent().getStringExtra(EXTRA_TARGET_ENTITY_NAME);
        triggerScope = (Trigger.Scope) getIntent().getSerializableExtra(EXTRA_SCOPE);

        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
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

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                super.onLoadFinished(loader, data);

                if (selectedListItem != null) {
                    listViewItemLongClicked(selectedListItem);
                }
            }
        };

        listView.setAdapter(triggerAdapter);
        listView.setEmptyView(findViewById(android.R.id.empty));

        getSupportLoaderManager().initLoader(0, null, cursorAdapterLoader);

        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_green_300)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTrigger();
            }
        });
    }

    @ItemLongClick(R.id.listView)
    void listViewItemLongClicked(int position) {
        selectedListItem = position;
        listView.setItemChecked(position, true);
        updateSelection();
    }

    @ItemClick(R.id.listView)
    void listViewItemClicked(Cursor cursor) {
        clearSelection();
        Trigger trigger = (Trigger) EntityMapper.TRIGGER_MAPPER.fromCursor(cursor);
        Intent intent = getTriggerActivityIntent(EditTriggerActivity_.class, trigger.getUri());
        startActivity(intent);
    }

    @OptionsItem(R.id.delete_item)
    void delete() {
        ConfirmDialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(DELETE_ACTION, getString(R.string.dialog_action_delete_trigger));
        confirmDialog.show(getFragmentManager(), "confirmDeleteTrigger");
    }

    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (actionId == DELETE_ACTION && dialogButton == DialogInterface.BUTTON_POSITIVE) {
            Trigger trigger = getSelectedListItem();
            clearSelection();
            provider.delete(trigger);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        deleteItem.setVisible(isSelected());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void restartLoader() {
        getSupportLoaderManager().restartLoader(0, null, cursorAdapterLoader);
    }

    @Override
    public void destroyLoader() {
        getSupportLoaderManager().destroyLoader(0);
    }

    private void addTrigger() {
        clearSelection();
        Intent intent = getTriggerActivityIntent(AddTriggerActivity_.class);
        startActivity(intent);
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
        if (isSelected()) {
            clearSelection();
        } else {
            onBackPressed(); // home behaves like back button - we need to return to vm from triggers
        }
    }

    @Override
    public void onBackPressed() {
        if (isSelected()) {
            clearSelection();
        } else {
            super.onBackPressed();
        }
    }

    private void clearSelection() {
        listView.setItemChecked(listView.getCheckedItemPosition(), false);
        selectedListItem = null;
        updateSelection();
    }

    private void updateSelection() {
        Trigger trigger = getSelectedListItem();
        setTitle(trigger != null ? getConditionString(trigger.getCondition()) : String.format(TITLE_FORMAT, getScopeText()));
        invalidateOptionsMenu();
    }

    private boolean isSelected() {
        return listView.getCheckedItemPosition() >= 0;
    }

    private Trigger getSelectedListItem() {
        int position = listView.getCheckedItemPosition();
        Trigger trigger = null;

        if (position >= 0) {
            Cursor cursor = (Cursor) listView.getItemAtPosition(position);
            trigger = (Trigger) EntityMapper.TRIGGER_MAPPER.fromCursor(cursor);
        }

        return trigger;
    }
}
