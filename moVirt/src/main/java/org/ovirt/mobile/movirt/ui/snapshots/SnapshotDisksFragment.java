package org.ovirt.mobile.movirt.ui.snapshots;

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
import org.ovirt.mobile.movirt.model.SnapshotDisk;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.listfragment.SnapshotBoundResumeSyncableBaseEntityListFragment;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.ItemName;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortEntry;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortOrderType;
import org.ovirt.mobile.movirt.util.usage.MemorySize;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.SnapshotDisk.NAME;
import static org.ovirt.mobile.movirt.provider.OVirtContract.SnapshotDisk.SIZE;
import static org.ovirt.mobile.movirt.provider.OVirtContract.SnapshotDisk.STATUS;

@EFragment(R.layout.fragment_base_entity_list)
public class SnapshotDisksFragment extends SnapshotBoundResumeSyncableBaseEntityListFragment<SnapshotDisk> {

    public SnapshotDisksFragment() {
        super(SnapshotDisk.class);
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
    public SortEntry[] getSortEntries() {
        return new SortEntry[]{
                new SortEntry(new ItemName(NAME), SortOrderType.A_TO_Z),
                new SortEntry(new ItemName(STATUS), SortOrderType.A_TO_Z)
        };
    }

    @Background
    @Receiver(actions = Broadcasts.IN_SYNC, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void syncingChanged(@Receiver.Extra(Broadcasts.Extras.SYNCING) boolean syncing) {
        if (syncing) {
            entityFacade.syncAll(getVmId(), getSnapshotId());
        }
    }

    @Background
    @Override
    public void onRefresh() {
        entityFacade.syncAll(new ProgressBarResponse<List<SnapshotDisk>>(this), getVmId(), getSnapshotId());
    }
}
