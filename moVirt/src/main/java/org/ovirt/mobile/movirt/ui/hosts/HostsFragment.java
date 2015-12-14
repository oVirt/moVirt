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
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.ui.BaseEntityListFragment;

@EFragment(R.layout.fragment_base_entity_list)
public class HostsFragment extends BaseEntityListFragment<Host> implements OVirtContract.Host {

    public HostsFragment() {
        super(Host.class);
    }

    @Override
    protected CursorAdapter createCursorAdapter() {
        SimpleCursorAdapter hostListAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.host_list_item,
                null,
                new String[]{NAME, STATUS},
                new int[]{R.id.host_name, R.id.host_status}, 0);

        hostListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(NAME)) {
                    TextView textView = (TextView) view;
                    String vmName = cursor.getString(cursor.getColumnIndex(NAME));
                    textView.setText(vmName);
                } else if (columnIndex == cursor.getColumnIndex(STATUS)) {
                    ImageView imageView = (ImageView) view;
                    Host.Status status = Host.Status.valueOf(cursor.getString(cursor.getColumnIndex(STATUS)));
                    imageView.setImageResource(status.getResource());
                }

                return true;
            }
        });

        return hostListAdapter;
    }
}

