package org.ovirt.mobile.movirt.ui.hosts;

import android.database.Cursor;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.enums.HostStatus;
import org.ovirt.mobile.movirt.ui.listfragment.ClusterBoundBaseListFragment;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.ItemName;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortEntry;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortOrderType;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Host.CPU_USAGE;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Host.MEMORY_USAGE;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Host.NAME;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Host.STATUS;

@EFragment(R.layout.fragment_base_entity_list)
public class HostsFragment extends ClusterBoundBaseListFragment<Host> {

    public HostsFragment() {
        super(Host.class);
    }

    @Override
    protected CursorAdapter createCursorAdapter() {
        SimpleCursorAdapter hostListAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.usage_stats_entity_list_item,
                null,
                new String[]{NAME, STATUS, CPU_USAGE},
                new int[]{R.id.name, R.id.status, R.id.statistics}, 0);

        hostListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(NAME)) {
                    TextView textView = (TextView) view;
                    String vmName = cursor.getString(cursor.getColumnIndex(NAME));
                    textView.setText(vmName);
                } else if (columnIndex == cursor.getColumnIndex(STATUS)) {
                    String status = cursor.getString(cursor.getColumnIndex(STATUS));
                    if (status != null) {
                        ImageView imageView = (ImageView) view;
                        HostStatus hostStatus = HostStatus.fromString(status);
                        imageView.setImageResource(hostStatus.getResource());
                    }
                } else if (columnIndex == cursor.getColumnIndex(CPU_USAGE)) {
                    TextView textView = (TextView) view;
                    double cpuUsage = cursor.getDouble(cursor.getColumnIndex(CPU_USAGE));
                    double memUsage = cursor.getDouble(cursor.getColumnIndex(MEMORY_USAGE));

                    textView.setText(getString(R.string.statistics, cpuUsage, memUsage));
                }

                return true;
            }
        });

        return hostListAdapter;
    }

    @Override
    public SortEntry[] getSortEntries() {
        return new SortEntry[]{
                new SortEntry(new ItemName(Host.NAME), SortOrderType.A_TO_Z),
                new SortEntry(new ItemName(Host.STATUS), SortOrderType.A_TO_Z),
                new SortEntry(new ItemName(Host.CPU_USAGE), SortOrderType.LOW_TO_HIGH),
                new SortEntry(new ItemName(Host.MEMORY_USAGE), SortOrderType.LOW_TO_HIGH)
        };
    }
}

