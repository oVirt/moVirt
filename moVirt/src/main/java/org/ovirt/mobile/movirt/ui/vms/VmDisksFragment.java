package org.ovirt.mobile.movirt.ui.vms;

import android.database.Cursor;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Receiver;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
import org.ovirt.mobile.movirt.auth.properties.property.version.support.VersionSupport;
import org.ovirt.mobile.movirt.facade.DiskAttachmentsFacade;
import org.ovirt.mobile.movirt.facade.DiskFacade;
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.model.DiskAttachment;
import org.ovirt.mobile.movirt.model.view.DiskAndAttachment;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.listfragment.VmBoundResumeSyncableBaseEntityListFragment;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.ItemName;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortEntry;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortOrderType;
import org.ovirt.mobile.movirt.util.usage.MemorySize;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.DiskAndAttachment.NAME;
import static org.ovirt.mobile.movirt.provider.OVirtContract.DiskAndAttachment.SIZE;
import static org.ovirt.mobile.movirt.provider.OVirtContract.DiskAndAttachment.STATUS;

@EFragment(R.layout.fragment_base_entity_list)
public class VmDisksFragment extends VmBoundResumeSyncableBaseEntityListFragment<DiskAndAttachment> {
    private static final String TAG = VmDisksFragment.class.getSimpleName();

    @Bean
    AccountPropertiesManager propertiesManager;

    @Bean
    DiskAttachmentsFacade diskAttachmentsFacade;

    @Bean
    DiskFacade diskFacade;

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
    @Receiver(actions = Broadcasts.IN_SYNC, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void syncingChanged(@Receiver.Extra(Broadcasts.Extras.SYNCING) boolean syncing) {
        if (syncing) {
            Version version = propertiesManager.getApiVersion();

            if (VersionSupport.VM_DISKS.isSupported(version)) {
                diskFacade.syncAll(getVmId());
            } else if (VersionSupport.DISK_ATTACHMENTS.isSupported(version)) {
                diskAttachmentsFacade.syncAll(getVmId());
            }
        }
    }

    @Background
    @Override
    public void onRefresh() {
        Version version = propertiesManager.getApiVersion();

        if (VersionSupport.VM_DISKS.isSupported(version)) {
            diskFacade.syncAll(new ProgressBarResponse<List<Disk>>(this), getVmId());
        } else if (VersionSupport.DISK_ATTACHMENTS.isSupported(version)) {
            diskAttachmentsFacade.syncAll(new ProgressBarResponse<List<DiskAttachment>>(this), getVmId());
        }
    }
}
