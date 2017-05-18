package org.ovirt.mobile.movirt.ui.vms;

import android.database.Cursor;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.enums.VmStatus;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.listfragment.ClusterBoundBaseListFragment;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.ItemName;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortEntry;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortOrderType;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.CPU_USAGE;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.HOST_ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.MEMORY_USAGE;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.NAME;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.STATUS;

@EFragment(R.layout.fragment_base_entity_list)
public class VmsFragment extends ClusterBoundBaseListFragment<Vm> {

    private static final String TAG = VmsFragment.class.getSimpleName();

    @InstanceState
    protected String hostId;

    public VmsFragment() {
        super(Vm.class);
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    @Override
    protected CursorAdapter createCursorAdapter() {
        SimpleCursorAdapter vmListAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.usage_stats_entity_list_item,
                null,
                new String[]{NAME, STATUS, CPU_USAGE},
                new int[]{R.id.name, R.id.status, R.id.statistics}, 0);
        vmListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
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
                        VmStatus vmStatus = VmStatus.valueOf(status);
                        imageView.setImageResource(vmStatus.getResource());
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

        return vmListAdapter;
    }

    @Override
    protected void appendQuery(ProviderFacade.QueryBuilder<Vm> query) {
        super.appendQuery(query);

        if (isSingle() && hostId != null) {
            query.where(HOST_ID, hostId);
        }
    }

    @Override
    public SortEntry[] getSortEntries() {
        return new SortEntry[]{
                new SortEntry(new ItemName(NAME), SortOrderType.A_TO_Z),
                new SortEntry(new ItemName(STATUS), SortOrderType.A_TO_Z),
                new SortEntry(new ItemName(CPU_USAGE), SortOrderType.LOW_TO_HIGH),
                new SortEntry(new ItemName(MEMORY_USAGE), SortOrderType.LOW_TO_HIGH)
        };
    }
}
