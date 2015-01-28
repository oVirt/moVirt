package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.rest.Disk;
import org.ovirt.mobile.movirt.rest.Disks;

/**
 * Created by sphoorti on 11/2/15.
 */
public class DiskListAdapter extends ArrayAdapter<Disk> {
    private static final String TAG = DiskListAdapter.class.getSimpleName();

    public  DiskListAdapter (Context context, int textViewResourceId, Disks ldisk) {
        super(context, textViewResourceId,ldisk.disk);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Long diskSizeMB = 0L;
        boolean diskSizeExceptionFlag = false;
        try {
            Disk disk = getItem(position);
            View v = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                v = inflater.inflate(R.layout.disk_list_item, null);

            } else {
                v = convertView;
            }

            TextView diskName = (TextView) v.findViewById(R.id.disk_name);
            TextView diskStatus = (TextView) v.findViewById(R.id.disk_status);
            TextView diskSize = (TextView) v.findViewById(R.id.disk_size);

            diskName.setText("Name : " + disk.name);
            diskStatus.setText("Status : " + disk.status.state);
            try {
                diskSizeMB = Long.parseLong(disk.size);
            }
            catch (Exception e) {
                diskSizeExceptionFlag = true;
            }
            if(!diskSizeExceptionFlag) {
                diskSizeMB = diskSizeMB / (1024 * 1024);
                diskSize.setText("Size : " + diskSizeMB + " MB");
            }
            else {
                diskSize.setText("N/A");
            }

            return v;
        } catch (Exception ex) {
            Log.e(TAG, "error", ex);
            return null;
        }
    }
}
