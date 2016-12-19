package org.ovirt.mobile.movirt.ui.storage;

import android.database.Cursor;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.ui.BaseEntityListFragment;

import static org.ovirt.mobile.movirt.provider.OVirtContract.StorageDomain.NAME;
import static org.ovirt.mobile.movirt.provider.OVirtContract.StorageDomain.STATUS;

@EFragment(R.layout.fragment_base_entity_list)
public class StorageDomainFragment extends BaseEntityListFragment<StorageDomain> {

    public StorageDomainFragment() {
        super(StorageDomain.class);
    }

    @Override
    protected CursorAdapter createCursorAdapter() {
        SimpleCursorAdapter storageDomainListAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.storage_domain_list_item,
                null,
                new String[]{NAME, STATUS},
                new int[]{R.id.storage_domain_name, R.id.storage_domain_status}, 0);

        storageDomainListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(NAME)) {
                    TextView textView = (TextView) view;
                    String name = cursor.getString(cursor.getColumnIndex(NAME));
                    textView.setText(name);
                } else if (columnIndex == cursor.getColumnIndex(STATUS)) {
                    ImageView imageView = (ImageView) view;
                    String statusString = cursor.getString(cursor.getColumnIndex(STATUS));
                    if (statusString != null) {
                        StorageDomain.Status status = StorageDomain.Status.valueOf(statusString);
                        imageView.setImageResource(status.getResource());
                    } else {
                        imageView.setImageResource(StorageDomain.Status.UNKNOWN.getResource());
                    }
                }

                return true;
            }
        });

        return storageDomainListAdapter;
    }
}
