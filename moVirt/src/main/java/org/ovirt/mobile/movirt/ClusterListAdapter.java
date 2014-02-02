package org.ovirt.mobile.movirt;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.ovirt.mobile.movirt.rest.Cluster;
import org.ovirt.mobile.movirt.rest.OVirtClient;

import java.util.ArrayList;
import java.util.List;

public class ClusterListAdapter extends BaseAdapter {

    public static final String TAG = ClusterListAdapter.class.getSimpleName();
    private OVirtClient client;

    List<String> clusterList = new ArrayList<>();
    private String noFilterPlaceholder;

    public ClusterListAdapter(OVirtClient client, String noFilterPlaceholder) {
        this.client = client;
        this.noFilterPlaceholder = noFilterPlaceholder;
    }

    public void fetchData() {
        Log.i(TAG, "Fetching cluster data ...");
        List<Cluster> clusters = client.getClusters();
        clusterList.clear();
        clusterList.add(null); // default field representing no filter
        for (Cluster cluster : clusters) {
            clusterList.add(cluster.getName());
            Log.i(TAG, "Fetched cluster: " + cluster.getName());
        }
    }

    @Override
    public int getCount() {
        return clusterList.size();
    }

    @Override
    public Object getItem(int i) {
        return clusterList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            view = inflater.inflate(R.layout.cluster_list_item, viewGroup, false);
        }

        String clusterName = clusterList.get(i);
        TextView textView = (TextView) view.findViewById(R.id.cluster_view);
        if (clusterName == null) {
            textView.setText(noFilterPlaceholder);
        } else {
            textView.setText(clusterName);
        }

        return view;
    }
}
