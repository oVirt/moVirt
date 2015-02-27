package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

import static org.ovirt.mobile.movirt.provider.OVirtContract.NamedEntity.NAME;


@EFragment(R.layout.fragment_cluster_drawer)
public class ClusterDrawerFragment extends ListFragment {

    @Bean
    ProviderFacade provider;

    @StringRes(R.string.all_clusters)
    String allClusters;

    private static final String[] PROJECTION = new String[] {OVirtContract.Cluster.NAME, OVirtContract.Cluster.ID};
    private MatrixCursor emptyClusterCursor;
    private DrawerLayout drawerLayout;

    public interface ClusterSelectedListener {
        void onClusterSelected(Cluster cluster);
    }

    ClusterSelectedListener clusterSelectedListener;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        clusterSelectedListener = (ClusterSelectedListener) activity;
    }

    @AfterViews
    void initListView() {
        emptyClusterCursor = new MatrixCursor(PROJECTION);
        emptyClusterCursor.addRow(new String[]{allClusters, null});


        SimpleCursorAdapter clusterListAdapter = new SimpleCursorAdapter(getActivity(),
                                                                         R.layout.cluster_list_item,
                                                                         null,
                                                                         PROJECTION,
                                                                         new int[]{R.id.cluster_view});

        CursorAdapterLoader cursorAdapterLoader = new CursorAdapterLoader(clusterListAdapter) {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return provider.query(Cluster.class).orderBy(NAME).asLoader();
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                super.onLoadFinished(loader, new MergeCursor(new Cursor[]{emptyClusterCursor, data}));
            }
        };

        clusterListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex != 0) {
                    // do not display id column
                    return true;
                }
                TextView textView = (TextView) view;
                String clusterName = cursor.getString(cursor.getColumnIndex(NAME)); // only names selected, thus only 1 column
                textView.setText(clusterName);

                return true;
            }
        });
        setListAdapter(clusterListAdapter);

        getLoaderManager().initLoader(0, null, cursorAdapterLoader);

    }

    public void initDrawerLayout(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;

        drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout,
                                                 R.drawable.ic_drawer,
                                                 R.string.navigation_drawer_open,
                                                 R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                getActivity().getActionBar().setTitle(getActivity().getTitle());
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getActivity().getActionBar().setTitle(getActivity().getTitle());
            }
        };

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerLayout.setDrawerListener(drawerToggle);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        drawerToggle.syncState();

    }

    @ItemClick(android.R.id.list)
    void selectCluster(Cursor cursor) {
        final Cluster cluster = EntityMapper.CLUSTER_MAPPER.fromCursor(cursor);
        clusterSelectedListener.onClusterSelected(cluster);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        drawerToggle.onConfigurationChanged(newConfig);
    }

    public ActionBarDrawerToggle getDrawerToggle() {
        return drawerToggle;
    }
}
