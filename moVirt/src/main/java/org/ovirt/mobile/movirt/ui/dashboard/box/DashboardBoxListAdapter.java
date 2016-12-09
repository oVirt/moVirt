package org.ovirt.mobile.movirt.ui.dashboard.box;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.dashboard.DashboardEntityStatus;
import org.ovirt.mobile.movirt.ui.dashboard.maps.DashboardPosition;

import java.util.List;

public class DashboardBoxListAdapter extends ArrayAdapter<DashboardBoxData> {
    private final LayoutInflater inflater;

    public DashboardBoxListAdapter(Context context) {
        super(context, R.layout.dashboard_box_list_item);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        ImageView entityImage = (ImageView) view.findViewById(R.id.entityImage);

        entityCount.setText(item.getEntityCountFormatStr(getContext()));
        entityImage.setImageResource(item.getEntityImageId());

        DashboardEntityStatus first = item.getStatusOnPosition(DashboardPosition.FIRST);
        DashboardEntityStatus second = item.getStatusOnPosition(DashboardPosition.SECOND);
        DashboardEntityStatus third = item.getStatusOnPosition(DashboardPosition.THIRD);

        if (first.getCount() == 0 && second.getCount() == 0 && third.getCount() == 0) {
            view.findViewById(R.id.normalLayout).setVisibility(View.GONE);
            view.findViewById(R.id.naLayout).setVisibility(View.VISIBLE); // NA
        } else {
            TextView firstStatusCount = (TextView) view.findViewById(R.id.firstStatusCount);
            TextView secondStatusCount = (TextView) view.findViewById(R.id.secondStatusCount);
            TextView thirdStatusCount = (TextView) view.findViewById(R.id.thirdStatusCount);

            ImageView firstStatusImage = (ImageView) view.findViewById(R.id.firstStatusImage);
            ImageView secondStatusImage = (ImageView) view.findViewById(R.id.secondStatusImage);
            ImageView thirdStatusImage = (ImageView) view.findViewById(R.id.thirdStatusImage);

            firstStatusCount.setText(String.valueOf(first.getCount()));
            firstStatusImage.setImageResource(first.getIconResourceId());
            secondStatusCount.setText(String.valueOf(second.getCount()));
            secondStatusImage.setImageResource(second.getIconResourceId());
            thirdStatusCount.setText(String.valueOf(third.getCount()));
            thirdStatusImage.setImageResource(third.getIconResourceId());
        }

        return view;
    }
}
