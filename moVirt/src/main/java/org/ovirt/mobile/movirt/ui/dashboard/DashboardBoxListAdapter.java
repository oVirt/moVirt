package org.ovirt.mobile.movirt.ui.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.ovirt.mobile.movirt.R;

import java.util.List;

public class DashboardBoxListAdapter extends ArrayAdapter<DashboardBoxData> {
    private final LayoutInflater inflater;

    public DashboardBoxListAdapter(Context context) {
        super(context, R.layout.dashboard_box_list_item);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<DashboardBoxData> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = inflater.inflate(R.layout.dashboard_box_list_item, parent, false);
        } else {
            view = convertView;
        }

        DashboardBoxData item = getItem(position);
        TextView entityCount = (TextView) view.findViewById(R.id.entityCount);
        TextView warningEventCount = (TextView) view.findViewById(R.id.warningEventCount);
        TextView alertEventCount = (TextView) view.findViewById(R.id.alertEventCount);
        TextView errorEventCount = (TextView) view.findViewById(R.id.errorEventCount);

        entityCount.setText(item.getEntityCountFormatStr(getContext()));

        view.findViewById(R.id.warningEventLayout).setVisibility(View.GONE);
        view.findViewById(R.id.alertEventLayout).setVisibility(View.GONE);
        view.findViewById(R.id.errorEventLayout).setVisibility(View.GONE);
        view.findViewById(R.id.normalEventLayout).setVisibility(View.GONE);
        if (item.getWarningEventCount() == 0 && item.getAlertEventCount() == 0 && item.getErrorEventCount() == 0) {
            view.findViewById(R.id.normalEventLayout).setVisibility(View.VISIBLE);
        } else {
            if (item.getWarningEventCount() > 0) {
                view.findViewById(R.id.warningEventLayout).setVisibility(View.VISIBLE);
                warningEventCount.setText(String.valueOf(item.getWarningEventCount()));
            }

            if (item.getAlertEventCount() > 0) {
                view.findViewById(R.id.alertEventLayout).setVisibility(View.VISIBLE);
                alertEventCount.setText(String.valueOf(item.getAlertEventCount()));
            }

            if (item.getErrorEventCount() > 0) {
                view.findViewById(R.id.errorEventLayout).setVisibility(View.VISIBLE);
                errorEventCount.setText(String.valueOf(item.getErrorEventCount()));
            }
        }

        return view;
    }
}
