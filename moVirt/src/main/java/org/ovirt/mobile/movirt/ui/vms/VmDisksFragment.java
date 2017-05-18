package org.ovirt.mobile.movirt.ui.vms;

import android.database.Cursor;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.AccountEnvironment;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
import org.ovirt.mobile.movirt.auth.properties.property.version.support.VersionSupport;
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.model.DiskAttachment;
import org.ovirt.mobile.movirt.model.view.DiskAndAttachment;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.listfragment.VmBoundResumeSyncableBaseListFragment;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.ItemName;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortEntry;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortOrderType;
import org.ovirt.mobile.movirt.util.usage.MemorySize;

import static org.ovirt.mobile.movirt.provider.OVirtContract.DiskAndAttachment.NAME;
import static org.ovirt.mobile.movirt.provider.OVirtContract.DiskAndAttachment.SIZE;
import static org.ovirt.mobile.movirt.provider.OVirtContract.DiskAndAttachment.STATUS;

@EFragment(R.layout.fragment_base_entity_list)
public class VmDisksFragment extends VmBoundResumeSyncableBaseListFragment<DiskAndAttachment> {

    public VmDisksFragment() {
        super(DiskAndAttachment.class);
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
    @Override
    public void onRefresh() {
        try {
            final AccountEnvironment environment = environmentStore.getEnvironment(account);
            Version version = environment.getVersion();

            if (VersionSupport.VM_DISKS.isSupported(version)) {
                environment.getFacade(Disk.class).syncAll(new ProgressBarResponse<>(this), getVmId());
            } else if (VersionSupport.DISK_ATTACHMENTS.isSupported(version)) {
                environment.getFacade(DiskAttachment.class).syncAll(new ProgressBarResponse<>(this), getVmId());
            }
        } catch (AccountDeletedException ignore) {
        }
    }
}
