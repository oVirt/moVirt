package org.ovirt.mobile.movirt.ui.vms;

import android.database.Cursor;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Receiver;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Nic;
import org.ovirt.mobile.movirt.ui.BaseEntityListFragment;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Nic.LINKED;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Nic.MAC_ADDRESS;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Nic.NAME;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Nic.PLUGGED;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Created by suomiy on 2/2/16.
 */
@EFragment(R.layout.fragment_base_entity_list)
public class VmNicsFragment extends BaseEntityListFragment<Nic> {
    private static final String TAG = VmNicsFragment.class.getSimpleName();

    private String vmId;

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


    public String getVmId() {
        return vmId;
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }

    @Background
    @Receiver(actions = Broadcasts.IN_SYNC, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void syncingChanged(@Receiver.Extra(Broadcasts.Extras.SYNCING) boolean syncing) {
        if (syncing) {
            if (!isEmpty(filterSnapshotId)) {
                entityFacade.syncAll(getVmId(), filterSnapshotId);
            }
        }
    }

    @Background
    @Override
    public void onRefresh() {
        String[] params = isEmpty(filterSnapshotId) ? new String[]{filterVmId} : new String[]{getVmId(), filterSnapshotId};
        entityFacade.syncAll(new ProgressBarResponse<List<Nic>>(this), params);
    }
}
