package org.ovirt.mobile.movirt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.rest.Vm;
import org.springframework.util.StringUtils;

import java.util.List;

public class VmListAdapter extends BaseAdapter {
    private static final String TAG = VmListAdapter.class.getSimpleName();
    private OVirtClient client;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    private String clusterName;

    List<Vm> vmList;

    public void fetchData() {
        if (clusterName != null) {
            vmList = client.getVms("cluster=" + clusterName).vms;
        } else {
            vmList = client.getVms().vms;
        }
    }

    public VmListAdapter(OVirtClient client) {
        this.client = client;
    }

    @Override
    public int getCount() {
        return vmList != null ? vmList.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return vmList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            view = inflater.inflate(R.layout.vm_list_item, viewGroup, false);
        }

        Vm vm = vmList.get(i);
        TextView textView = (TextView) view.findViewById(R.id.vm_view);
        textView.setText(vm.name);

        return view;
    }
}
