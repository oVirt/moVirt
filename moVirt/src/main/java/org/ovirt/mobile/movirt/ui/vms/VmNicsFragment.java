package org.ovirt.mobile.movirt.ui.vms;

import android.database.Cursor;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Nic;
import org.ovirt.mobile.movirt.ui.BaseEntityListFragment;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Nic.LINKED;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Nic.MAC_ADDRESS;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Nic.NAME;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Nic.PLUGGED;

/**
 * Created by suomiy on 2/2/16.
 */
@EFragment(R.layout.fragment_base_entity_list)
public class VmNicsFragment extends BaseEntityListFragment<Nic> {
    private static final String TAG = VmNicsFragment.class.getSimpleName();

    public VmNicsFragment() {
        super(Nic.class);
    }

    @Override
    protected CursorAdapter createCursorAdapter() {
        SimpleCursorAdapter nicListAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.nic_list_item,
                null,
                new String[]{NAME, LINKED, PLUGGED},
                new int[]{R.id.nic_mac, R.id.nic_status, R.id.nic_para}, 0);
        nicListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(NAME)) {
                    TextView textView = (TextView) view;
                    String name = cursor.getString(columnIndex);
                    String mac = cursor.getString(cursor.getColumnIndex(MAC_ADDRESS));
                    textView.setText(getString(R.string.nic_name_and_address, name, mac));
                } else if (columnIndex == cursor.getColumnIndex(LINKED)) {
                    ImageView imageView = (ImageView) view;
                    boolean linked = cursor.getInt(columnIndex) > 0;
                    boolean plugged = cursor.getInt(cursor.getColumnIndex(PLUGGED)) > 0;
                    imageView.setImageResource((linked && plugged) ? R.drawable.icn_play : R.drawable.icn_stop);
                } else if (columnIndex == cursor.getColumnIndex(PLUGGED)) {
                    TextView textView = (TextView) view;
                    boolean plugged = cursor.getInt(columnIndex) > 0;
                    boolean linked = cursor.getInt(cursor.getColumnIndex(LINKED)) > 0;
                    textView.setText(getString(R.string.nic_para, linked, plugged));
                }

                return true;
            }
        });

        return nicListAdapter;
    }

    @Background
    @Override
    public void onRefresh() {
        entityFacade.syncAll(new ProgressBarResponse<List<Nic>>(this), super.filterVmId);
    }
}
