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
import org.ovirt.mobile.movirt.model.enums.StorageDomainStatus;
import org.ovirt.mobile.movirt.ui.listfragment.BaseEntityListFragment;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.ItemName;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortEntry;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortOrderType;

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
                    imageView.setImageResource(StorageDomainStatus.fromString(statusString).getResource());
                }

                return true;
            }
        });

        return storageDomainListAdapter;
    }

    @Override
    public SortEntry[] getSortEntries() {
        return new SortEntry[]{
                new SortEntry(new ItemName(StorageDomain.NAME), SortOrderType.A_TO_Z),
                new SortEntry(new ItemName(StorageDomain.STATUS), SortOrderType.A_TO_Z)
        };
    }
}
