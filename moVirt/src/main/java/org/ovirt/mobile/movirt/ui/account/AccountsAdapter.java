package org.ovirt.mobile.movirt.ui.account;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;

class AccountsAdapter extends ArrayAdapter<AccountWrapper> {

    private EditAccountsActivity editAccountsActivity;
    private ActiveSelection activeSelection;

    public AccountsAdapter(EditAccountsActivity editAccountsActivity, Context context, AccountWrapper[] objects) {
        super(context, R.layout.account_item, objects);
        this.editAccountsActivity = editAccountsActivity;
    }

    public void setActiveAccount(ActiveSelection activeSelection) {
        this.activeSelection = activeSelection;
    }

    public ActiveSelection getActiveSelection() {
        return activeSelection;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AccountWrapper wrapper = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.account_item, parent, false);
        }

        if (wrapper != null) {
            TextView textView = (TextView) convertView.findViewById(R.id.name);
            StringBuilder sr = new StringBuilder(wrapper.account.getName());

            if (wrapper.version != Version.V3) { // do not display default == not logged in
                sr.append(" ").append(editAccountsActivity.getString(R.string.version, wrapper.version));
            }

            if (activeSelection != null && activeSelection.isAccount(wrapper.account)) {
                sr.append(" ").append(editAccountsActivity.getString(R.string.current));
            }

            textView.setText(sr.toString());
        }

        return convertView;
    }
}
