package org.ovirt.mobile.movirt.ui.vms;

import android.database.Cursor;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.ui.BaseEntityListFragment;

@EFragment(R.layout.fragment_base_entity_list)
public class VmsFragment extends BaseEntityListFragment<Vm> implements OVirtContract.Vm {

    private static final String TAG = VmsFragment.class.getSimpleName();

    public VmsFragment() {
        super(Vm.class);
    }

    @Override
    protected CursorAdapter createCursorAdapter() {
        SimpleCursorAdapter vmListAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.vm_list_item,
                null,
                new String[]{NAME, STATUS},
                new int[]{R.id.vm_name, R.id.vm_status}, 0);

        vmListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(NAME)) {
                    TextView textView = (TextView) view;
                    String vmName = cursor.getString(cursor.getColumnIndex(NAME));
                    textView.setText(vmName);
                } else if (columnIndex == cursor.getColumnIndex(STATUS)) {
                    ImageView imageView = (ImageView) view;
                    Vm.Status status = Vm.Status.valueOf(cursor.getString(cursor.getColumnIndex(STATUS)));
                    imageView.setImageResource(status.getResource());
                }

                return true;
            }
        });

        return vmListAdapter;
    }
}
