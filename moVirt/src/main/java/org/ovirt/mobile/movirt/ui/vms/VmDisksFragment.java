package org.ovirt.mobile.movirt.ui.vms;

import android.database.Cursor;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Receiver;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.ResumeSyncableBaseEntityListFragment;
import org.ovirt.mobile.movirt.util.MemorySize;

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

    @Bean
    MovirtAuthenticator authenticator;

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
                    long size = cursor.getLong(columnIndex);
                    String sizeText = (size == -1) ? getString(R.string.disk_unknown_size) : new MemorySize(size).toString();
                    textView.setText(sizeText);
                } else if (columnIndex == cursor.getColumnIndex(STATUS)) {
                    String status = cursor.getString(columnIndex);
                    textView.setText(status == null ? getString(R.string.NA) : status.toUpperCase());
                }

                return true;
            }
        });

        return diskListAdapter;
    }

    @Override
    public boolean isResumeSyncable() {
        return authenticator.isV4Api() || isSnapshotFragment(); //we fetch disks with vm in v3 API
    }

    @Background
    @Receiver(actions = Broadcasts.IN_SYNC, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void syncingChanged(@Receiver.Extra(Broadcasts.Extras.SYNCING) boolean syncing) {
        if (syncing && isSnapshotFragment()) {
            entityFacade.syncAll(filterVmId, filterSnapshotId);
        }
    }

    @Background
    @Override
    public void onRefresh() {
        String[] params = isSnapshotFragment() ? new String[]{filterVmId, filterSnapshotId} : new String[]{filterVmId};
        entityFacade.syncAll(new ProgressBarResponse<List<Disk>>(this), params);
    }
}
