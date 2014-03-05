package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.StringRes;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.EntityType;
import org.ovirt.mobile.movirt.model.Trigger;
import org.ovirt.mobile.movirt.provider.OVirtContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@EActivity(R.layout.activity_edit_triggers)
public class EditTriggersActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, AddTriggerDialogFragment.AddTriggerActivity {
    public static final String EXTRA_TARGET_ENTITY_ID = "target_entity";
    public static final String EXTRA_TARGET_ENTITY_NAME = "target_name";

    private static final String[] PROJECTION = new String[] {
            OVirtContract.Trigger._ID,
            OVirtContract.Trigger.CONDITION,
            OVirtContract.Trigger.NOTIFICATION,
            OVirtContract.Trigger.SCOPE,
    };

    private String targetEntityId;
    private String targetEntityName;

    private Trigger.Scope triggerScope;

    private SimpleCursorAdapter triggerAdapter;

    @StringRes(R.string.whole_datacenter)
    String GLOBAL_SCOPE;

    @StringRes(R.string.cluster_scope)
    String CLUSTER_SCOPE;

    @AfterViews
    void init() {
        targetEntityId = getIntent().getStringExtra(EXTRA_TARGET_ENTITY_ID);
        targetEntityName = getIntent().getStringExtra(EXTRA_TARGET_ENTITY_NAME);

        triggerScopeLabel.setText(targetEntityId == null ? GLOBAL_SCOPE : String.format(CLUSTER_SCOPE, targetEntityName));

        triggerAdapter = new SimpleCursorAdapter(this,
                                                 R.layout.trigger_item,
                                                 null,
                                                 PROJECTION,
                                                 new int[] {R.id.trigger_id});
        triggerAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                return false;
            }
        });

        triggersListView.setAdapter(triggerAdapter);
        triggersListView.setEmptyView(findViewById(android.R.id.empty));

        getLoaderManager().initLoader(0, null, this);
    }

    @ViewById
    TextView triggerScopeLabel;

    @ViewById
    ListView triggersListView;

    @Click
    void addTrigger() {
        AddTriggerDialogFragment dialog = new AddTriggerDialogFragment();
        dialog.show(getFragmentManager(), "");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                                OVirtContract.Trigger.CONTENT_URI,
                                PROJECTION,
                                getTriggerSelection(),
                                getTriggerSelectionArgs(),
                                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        triggerAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        triggerAdapter.swapCursor(null);
    }

    private String getTriggerSelection() {
        return OVirtContract.Trigger.ENTITY_TYPE + " = ? AND " +
               OVirtContract.Trigger.SCOPE + " = ? AND " +
               OVirtContract.Trigger.TARGET_ID + (getTargetId() == null ? " IS NULL" : " = ?");
    }

    private String[] getTriggerSelectionArgs() {
        List<String> args = new ArrayList();
        args.add(getEntityType().toString());
        args.add(getScope().toString());
        if (getTargetId() != null) {
            args.add(getTargetId());
        }
        return args.toArray(new String[args.size()]);
    }

    public EntityType getEntityType() {
        return EntityType.VM;
    }

    public Trigger.Scope getScope() {
        return targetEntityId == null ? Trigger.Scope.GLOBAL : Trigger.Scope.CLUSTER;
    }

    public String getTargetId() {
        return targetEntityId;
    }
}
