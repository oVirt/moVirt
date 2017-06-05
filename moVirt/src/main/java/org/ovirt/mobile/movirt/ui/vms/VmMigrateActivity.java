package org.ovirt.mobile.movirt.ui.vms;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
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
import org.ovirt.mobile.movirt.auth.account.data.Selection;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.enums.HostStatus;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.Relation;
import org.ovirt.mobile.movirt.ui.ActionBarLoaderActivity;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

import static org.ovirt.mobile.movirt.Constants.APP_PACKAGE_DOT;

@EActivity(R.layout.activity_migrate_vm)
public class VmMigrateActivity extends ActionBarLoaderActivity {

    public static final String EXTRA_CLUSTER_ID = APP_PACKAGE_DOT + "EXTRA_CLUSTER_ID";
    public static final String EXTRA_HOST_ID = APP_PACKAGE_DOT + "EXTRA_HOST_ID";
    public static final String EXTRA_SELECTION = APP_PACKAGE_DOT + "EXTRA_SELECTION";
    public static final int RESULT_DEFAULT = RESULT_FIRST_USER;
    public static final int RESULT_SELECT = RESULT_FIRST_USER + 1;
    public static final
    String RESULT_EXTRA_HOST_ID = APP_PACKAGE_DOT + "RESULT_EXTRA_HOST_ID";
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

    @ViewById
    public TextView statusText;

    private SimpleCursorAdapter hostsAdapter;
    private CursorAdapterLoader cursorAdapterLoader;
    private String currentHostId;
    private String currentClusterId;
    private Host selectedHost;

    @AfterViews
    void init() {
        statusText.setText(((Selection) getIntent().getParcelableExtra(EXTRA_SELECTION)).getDescription());

        getExtras();
        setLoader(currentClusterId, currentHostId);
        setList();
    }

    @Override
    public void restartLoader() {
        getSupportLoaderManager().restartLoader(HOSTS_LOADER, null, cursorAdapterLoader);
    }

    @Override
    public void destroyLoader() {
        getSupportLoaderManager().destroyLoader(HOSTS_LOADER);
    }

    private void getExtras() {
        Intent intent = getIntent();
        currentClusterId = intent.getStringExtra(EXTRA_CLUSTER_ID);
        currentHostId = intent.getStringExtra(EXTRA_HOST_ID);
    }

    private void setLoader(final String filterClusterId, final String filterHostId) {
        hostsAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_activated_1, null,
                new String[]{OVirtContract.Host.NAME}, new int[]{android.R.id.text1}, 0);
        cursorAdapterLoader = new CursorAdapterLoader(hostsAdapter) {
            @Override
            public synchronized Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return provider.query(Host.class)
                        .where(Host.STATUS, HostStatus.UP.toString())
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
        listHosts.setOnItemClickListener((parent, view, position, id) -> {
            Cursor cursor = hostsAdapter.getCursor();
            if (cursor != null && cursor.moveToPosition(position)) {
                selectedHost = new Host();
                selectedHost.initFromCursor(cursor);
                buttonMigrateToSelected.setEnabled(true);
            }
        });
    }

    private void enableViews() {
        labelEmpty.setVisibility(View.GONE);
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
        result.putExtra(RESULT_EXTRA_HOST_ID, selectedHost.getId());
        setResult(RESULT_SELECT, result);
        finish();
    }
}
