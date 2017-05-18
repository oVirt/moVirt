package org.ovirt.mobile.movirt.ui.account;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
import org.ovirt.mobile.movirt.ui.BroadcastAwareAppCompatActivity;
import org.ovirt.mobile.movirt.ui.SettingsActivity_;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.util.Disposables;
import org.ovirt.mobile.movirt.util.message.MessageHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@EActivity(R.layout.activity_edit_accounts)
@OptionsMenu(R.menu.delete_item)
public class EditAccountsActivity extends BroadcastAwareAppCompatActivity implements ConfirmDialogFragment.ConfirmDialogListener {

    private static final int DELETE_ACTION = 0;

    @ViewById
    FloatingActionButton fab;

    @StringRes(R.string.engines)
    String TITLE;

    @OptionsMenuItem
    MenuItem deleteItem;

    @ViewById
    ListView listView;

    @Bean
    AccountRxStore accountRxStore;

    @Bean
    MessageHelper messageHelper;

    @Bean
    EnvironmentStore envStore;

    @InstanceState
    Integer selectedListItem;

    private Disposables disposables = new Disposables();

    @AfterViews
    void init() {
        setTitle(TITLE);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_green_300)));
        fab.setOnClickListener(view -> addAccount());
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        // TODO refactor to MVP
        disposables.add(accountRxStore.ALL_ACCOUNTS.distinctUntilChanged()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accounts -> {
                    AccountsAdapter oldAdapter = (AccountsAdapter) listView.getAdapter();
                    Collection<MovirtAccount> accs = accounts.getAccounts();
                    List<AccountWrapper> wrappers = new ArrayList<>(accs.size());

                    for (MovirtAccount account : accs) {
                        try {
                            wrappers.add(new AccountWrapper(account, envStore.getVersion(account)));
                        } catch (AccountDeletedException ignore) {
                        }
                    }

                    final AccountsAdapter adapter = new AccountsAdapter(getApplicationContext(), wrappers.toArray(new AccountWrapper[wrappers.size()]));
                    if (oldAdapter != null) {
                        adapter.setActiveAccount(oldAdapter.getActiveSelection());
                    }
                    listView.setAdapter(adapter);

                    if (selectedListItem != null) {
                        listViewItemLongClicked(selectedListItem);
                    }
                }));

        disposables.add(accountRxStore.ACTIVE_SELECTION.distinctUntilChanged()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(activeAccount -> {
                    final AccountsAdapter adapter = (AccountsAdapter) listView.getAdapter();
                    if (adapter != null) {
                        adapter.setActiveAccount(activeAccount);
                        adapter.notifyDataSetChanged();
                    }
                }));
    }

    @Override
    protected void onDestroy() {
        disposables.destroy();
        super.onDestroy();
    }

    @ItemLongClick(R.id.listView)
    void listViewItemLongClicked(int position) {
        selectedListItem = position;
        listView.setItemChecked(position, true);
        updateSelection();
    }

    @ItemClick(R.id.listView)
    void listViewItemClicked(AccountWrapper wrapper) {
        clearSelection();
        Intent intent = new Intent(this, SettingsActivity_.class);
        intent.putExtra(Constants.ACCOUNT_KEY, wrapper.account);
        startActivity(intent);
    }

    @OptionsItem(R.id.delete_item)
    void delete() {
        ConfirmDialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(DELETE_ACTION, getString(R.string.dialog_action_delete_account));
        confirmDialog.show(getFragmentManager(), "confirmDelete");
    }

    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (actionId == DELETE_ACTION && dialogButton == DialogInterface.BUTTON_POSITIVE) {
            MovirtAccount account = getSelectedAccount();
            clearSelection();
            accountRxStore.removeAccount(account);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        deleteItem.setVisible(isSelected());
        return super.onPrepareOptionsMenu(menu);
    }

    private void addAccount() {
        clearSelection();
        Intent intent = new Intent(getApplicationContext(), AddAccountActivity_.class);
        startActivity(intent);
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        if (isSelected()) {
            clearSelection();
        } else {
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        if (isSelected()) {
            clearSelection();
        } else {
            super.onBackPressed();
        }
    }

    private void clearSelection() {
        listView.setItemChecked(listView.getCheckedItemPosition(), false);
        selectedListItem = null;
        updateSelection();
    }

    private void updateSelection() {
        MovirtAccount account = getSelectedAccount();
        setTitle(account != null ? account.getName() : TITLE);
        invalidateOptionsMenu();
    }

    private boolean isSelected() {
        return listView.getCheckedItemPosition() >= 0;
    }

    private MovirtAccount getSelectedAccount() {
        int position = listView.getCheckedItemPosition();
        AccountWrapper wrapper = (AccountWrapper) listView.getItemAtPosition(position);
        return wrapper == null ? null : wrapper.account;
    }

    private class AccountsAdapter extends ArrayAdapter<AccountWrapper> {

        private ActiveSelection activeSelection;

        public AccountsAdapter(Context context, AccountWrapper[] objects) {
            super(context, R.layout.account_item, objects);
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
                    sr.append(" ").append(getString(R.string.version, wrapper.version));
                }

                if (activeSelection != null && activeSelection.isAccount(wrapper.account)) {
                    sr.append(" ").append(getString(R.string.current));
                }

                textView.setText(sr.toString());
            }

            return convertView;
        }
    }

    protected static class AccountWrapper {
        MovirtAccount account;
        Version version;

        public AccountWrapper(MovirtAccount account, Version version) {
            this.account = account;
            this.version = version;
        }
    }
}
