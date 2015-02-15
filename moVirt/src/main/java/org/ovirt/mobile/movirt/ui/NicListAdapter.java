package org.ovirt.mobile.movirt.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.sync.rest.Nic;
import org.ovirt.mobile.movirt.sync.rest.Nics;

/**
 * Created by yixin on 11/2/15.
 */
public class NicListAdapter extends ArrayAdapter<Nic> {
    private static final String TAG = NicListAdapter.class.getSimpleName();

    public NicListAdapter(Context context, int textViewResourceId, Nics lnic) {
        super(context, textViewResourceId,lnic.nic);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            Nic nic = getItem(position);
            View v = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                v = inflater.inflate(R.layout.nic_list_item, null);

            } else {
                v = convertView;
            }

            TextView nicMac = (TextView) v.findViewById(R.id.nic_mac);
            ImageView nicStatus = (ImageView) v.findViewById(R.id.nic_status);
            TextView nicPara = (TextView) v.findViewById(R.id.nic_para);

            nicMac.setText(getContext().getString(R.string.nic_name_and_address, nic.name, nic.mac.address));
            if (nic.linked && nic.plugged) {
                nicStatus.setImageResource(R.drawable.icn_play);
            } else {
                nicStatus.setImageResource(R.drawable.icn_stop);
            }
            nicPara.setText(getContext().getString(R.string.nic_para, nic.linked, nic.plugged));

            return v;
        } catch (Exception ex) {
            Log.e(TAG, "error", ex);
            return null;
        }
    }
}
