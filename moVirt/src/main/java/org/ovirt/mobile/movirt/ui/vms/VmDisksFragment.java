package org.ovirt.mobile.movirt.ui.vms;

import android.database.Cursor;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Receiver;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.ResumeSyncableBaseEntityListFragment;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Disk.NAME;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Disk.SIZE;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Disk.STATUS;

/**
 * Created by suomiy on 2/2/16.
 */
@EFragment(R.layout.fragment_base_entity_list)
public class VmDisksFragment extends ResumeSyncableBaseEntityListFragment<Disk> {
    private static final String TAG = VmDisksFragment.class.getSimpleName();

    private String vmId;

    public VmDisksFragment() {
        super(Disk.class);
    }

    @Override
    protected CursorAdapter createCursorAdapter() {
        SimpleCursorAdapter diskListAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.disk_list_item,
                null,
                new String[]{NAME, SIZE, STATUS},
                new int[]{R.id.disk_name, R.id.disk_size, R.id.disk_status}, 0);
        diskListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                TextView textView = (TextView) view;

                if (columnIndex == cursor.getColumnIndex(NAME)) {
                    String name = cursor.getString(columnIndex);
                    textView.setText(name);
                } else if (columnIndex == cursor.getColumnIndex(SIZE)) {
                    String size = cursor.getString(columnIndex);
                    try {
                        Long diskSizeMB = Long.parseLong(size);
                        diskSizeMB = diskSizeMB / (1024 * 1024);
                        size = getString(R.string.disk_size, diskSizeMB);
                    } catch (Exception e) {
                        size = getString(R.string.disk_unknown_size);
                    }
                    textView.setText(size);
                } else if (columnIndex == cursor.getColumnIndex(STATUS)) {
                    String status = cursor.getString(columnIndex);
                    textView.setText(status.toUpperCase());
                }

                return true;
            }
        });

        return diskListAdapter;
    }

    public String getVmId() {
        return vmId;
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    @Override
    public boolean isResumeSyncable() {
        return isSnapshotFragment();
    }

    @Background
    @Receiver(actions = Broadcasts.IN_SYNC, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void syncingChanged(@Receiver.Extra(Broadcasts.Extras.SYNCING) boolean syncing) {
        if (syncing && isSnapshotFragment()) {
            entityFacade.syncAll(getVmId(), filterSnapshotId);
        }
    }

    @Background
    @Override
    public void onRefresh() {
        String[] params = isSnapshotFragment() ? new String[]{getVmId(), filterSnapshotId} : new String[]{filterVmId};
        entityFacade.syncAll(new ProgressBarResponse<List<Disk>>(this), params);
    }
}
