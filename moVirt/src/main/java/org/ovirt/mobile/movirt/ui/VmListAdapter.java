package org.ovirt.mobile.movirt.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.rest.Vm;

public class VmListAdapter extends ArrayAdapter<Vm> {
    private static final String TAG = VmListAdapter.class.getSimpleName();

    public VmListAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            view = inflater.inflate(R.layout.vm_list_item, viewGroup, false);
        }

        Vm vm = getItem(i);
        TextView textView = (TextView) view.findViewById(R.id.vm_view);
        textView.setText(vm.getName() + " (status: " + vm.getStatus() + ")");

        return view;
    }
}
