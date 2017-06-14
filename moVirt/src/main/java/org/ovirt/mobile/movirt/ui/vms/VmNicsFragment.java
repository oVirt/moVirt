package org.ovirt.mobile.movirt.ui.vms;

import android.database.Cursor;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.properties.property.version.support.VersionSupport;
import org.ovirt.mobile.movirt.model.Nic;
import org.ovirt.mobile.movirt.ui.listfragment.VmBoundResumeSyncableBaseListFragment;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.ItemName;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortEntry;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortOrderType;
import org.ovirt.mobile.movirt.util.CursorHelper;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Nic.LINKED;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Nic.MAC_ADDRESS;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Nic.NAME;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Nic.PLUGGED;

@EFragment(R.layout.fragment_base_entity_list)
public class VmNicsFragment extends VmBoundResumeSyncableBaseListFragment<Nic> {

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
                    CursorHelper cursorHelper = new CursorHelper(cursor);
                    boolean linked = cursorHelper.getBoolean(columnIndex);
                    boolean plugged = cursorHelper.getBoolean(cursor.getColumnIndex(PLUGGED));
                    imageView.setImageResource((linked && plugged) ? R.drawable.icn_play : R.drawable.icn_stop);
                } else if (columnIndex == cursor.getColumnIndex(PLUGGED)) {
                    TextView textView = (TextView) view;
                    CursorHelper cursorHelper = new CursorHelper(cursor);
                    boolean plugged = cursorHelper.getBoolean(columnIndex);
                    boolean linked = cursorHelper.getBoolean(cursor.getColumnIndex(LINKED));
                    textView.setText(getString(R.string.nic_para, linked, plugged));
                }

                return true;
            }
        });

        return nicListAdapter;
    }

    @Override
    public SortEntry[] getSortEntries() {
        return new SortEntry[]{
                new SortEntry(new ItemName(NAME), SortOrderType.A_TO_Z)
        };
    }

    @Override
    public boolean isResumeSyncable() {
        try {
            return !VersionSupport.NICS_POLLED_WITH_VMS.isSupported(environmentStore.getVersion(account));
        } catch (AccountDeletedException e) {
            return false;
        }
    }
}
