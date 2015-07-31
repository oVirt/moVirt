package org.ovirt.mobile.movirt.ui.vms;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.Relation;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

@EActivity(R.layout.activity_migrate_vm)
public class VmMigrateActivity extends ActionBarActivity {

    public static final String CLUSTER_ID_EXTRA = "org.ovirt.mobile.movirt.CLUSTER_ID_EXTRA";
    public static final String HOST_ID_EXTRA = "org.ovirt.mobile.movirt.HOST_ID_EXTRA";
    public static final int RESULT_DEFAULT = RESULT_FIRST_USER;
    public static final int RESULT_SELECT = RESULT_FIRST_USER + 1;
    public static final
    String RESULT_HOST_ID_EXTRA = "org.ovirt.mobile.movirt.RESULT_HOST_ID_EXTRA";
    private static final int HOSTS_LOADER = 0;
    @ViewById
    TextView labelEmpty;
    @ViewById
    ListView listHosts;
    @ViewById
    Button buttonMigrateToDefault;
    @ViewById
    Button buttonMigrateToSelected;
    @Bean
    ProviderFacade provider;

    private SimpleCursorAdapter hostsAdapter;
    private CursorAdapterLoader cursorAdapterLoader;
    private String currentHostId;
    private String currentClusterId;
    private Host selectedHost;

    @AfterViews
    void init() {
        getExtras();
        setLoader(currentClusterId, currentHostId);
        setList();
    }

    private void getExtras() {
        Intent intent = getIntent();
        currentClusterId = intent.getStringExtra(CLUSTER_ID_EXTRA);
        currentHostId = intent.getStringExtra(HOST_ID_EXTRA);
    }

    private void setLoader(final String filterClusterId, final String filterHostId) {
        hostsAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_activated_1, null,
                new String[]{OVirtContract.Host.NAME}, new int[]{android.R.id.text1}, 0);
        cursorAdapterLoader = new CursorAdapterLoader(hostsAdapter) {
            @Override
            public synchronized Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return provider.query(Host.class)
                        .where(Host.STATUS, Host.Status.UP.toString())
                        .where(Host.CLUSTER_ID, filterClusterId)
                        .where(Host.ID, filterHostId, Relation.NOT_EQUAL).asLoader();
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                hostsAdapter.swapCursor(data);
                if (data != null && data.getCount() > 0) {
                    enableViews();
                }
            }
        };
        getSupportLoaderManager().initLoader(HOSTS_LOADER, null, cursorAdapterLoader);
    }

    private void setList() {
        listHosts.setAdapter(hostsAdapter);
        listHosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = hostsAdapter.getCursor();
                Object item = listHosts.getSelectedItem();
                if (cursor != null && cursor.moveToPosition(position)) {
                    selectedHost = new Host();
                    selectedHost.initFromCursor(cursor);
                    buttonMigrateToSelected.setEnabled(true);
                }
            }
        });
    }

    private void enableViews() {
        labelEmpty.setVisibility(View.INVISIBLE);
        listHosts.setVisibility(View.VISIBLE);
        buttonMigrateToDefault.setEnabled(true);
    }

    @Click(R.id.buttonMigrateToDefault)
    public void clickMigrateToDefault() {
        setResult(RESULT_DEFAULT);
        finish();
    }

    @Click(R.id.buttonMigrateToSelected)
    public void clickMigrateToSelected() {
        Intent result = new Intent();
        result.putExtra(RESULT_HOST_ID_EXTRA, selectedHost.getId());
        setResult(RESULT_SELECT, result);
        finish();
    }
}
