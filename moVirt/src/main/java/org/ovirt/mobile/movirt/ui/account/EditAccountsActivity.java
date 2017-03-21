package org.ovirt.mobile.movirt.ui.account;

import android.accounts.Account;
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
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.data.ActiveAccount;
import org.ovirt.mobile.movirt.ui.BroadcastAwareAppCompatActivity;
import org.ovirt.mobile.movirt.ui.SettingsActivity;
import org.ovirt.mobile.movirt.ui.SettingsActivity_;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.util.RxHelper;
import org.ovirt.mobile.movirt.util.message.MessageHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

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

    @OptionsMenuItem
    MenuItem activateItem;

    @ViewById
    ListView listView;

    @Bean
    AccountRxStore accountRxStore;

    @Bean
    MessageHelper messageHelper;

    @InstanceState
    Integer selectedListItem;

    private List<Disposable> disposables = new ArrayList<>();

    @AfterViews
    void init() {
        setTitle(TITLE);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_green_300)));
        fab.setOnClickListener(view -> addAccount());
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
    }

    @Override
    protected void onPostResume() {
        disposables.add(accountRxStore.ALL_ACCOUNTS.subscribe(accounts -> {
            ArrayAdapter<Account> adapter = new AccountsAdapter(getApplicationContext(), accounts);
            listView.setAdapter(adapter);

            if (selectedListItem != null) {
                listViewItemLongClicked(selectedListItem);
            }
        }));

        disposables.add(accountRxStore.ACTIVE_ACCOUNT.subscribe(activeAccount -> {
            final AccountsAdapter adapter = (AccountsAdapter) listView.getAdapter();
            if (adapter != null) {
                adapter.setActiveAccount(activeAccount);
                adapter.notifyDataSetChanged();
            }
        }));
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        RxHelper.dispose(disposables);
        super.onPause();
    }

    @ItemLongClick(R.id.listView)
    void listViewItemLongClicked(int position) {
        selectedListItem = position;
        listView.setItemChecked(position, true);
        updateSelection();
    }

    @ItemClick(R.id.listView)
    void listViewItemClicked(Account account) {
        clearSelection();
        Intent intent = new Intent(this, SettingsActivity_.class);
        intent.putExtra(SettingsActivity.ACCOUNT_KEY, account);
        startActivity(intent);
    }

    @OptionsItem(R.id.delete_item)
    void delete() {
        ConfirmDialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(DELETE_ACTION, getString(R.string.dialog_action_delete_account));
        confirmDialog.show(getFragmentManager(), "confirmDelete");
    }

    @OptionsItem(R.id.activate_item)
    void activate() {
        Account account = getSelectedListItem();
        clearSelection();
        accountRxStore.ACTIVE_ACCOUNT.onNext(new ActiveAccount(account));
    }

    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (actionId == DELETE_ACTION && dialogButton == DialogInterface.BUTTON_POSITIVE) {
            Account account = getSelectedListItem();
            clearSelection();
            accountRxStore.removeAccount(account);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        deleteItem.setVisible(isSelected());
        activateItem.setVisible(isSelected());
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
        Account account = getSelectedListItem();
        setTitle(account != null ? account.name : TITLE);
        invalidateOptionsMenu();
    }

    private boolean isSelected() {
        return listView.getCheckedItemPosition() >= 0;
    }

    private Account getSelectedListItem() {
        int position = listView.getCheckedItemPosition();
        return (Account) listView.getItemAtPosition(position);
    }

    private class AccountsAdapter extends ArrayAdapter<Account> {

        private ActiveAccount activeAccount;

        public AccountsAdapter(Context context, Account[] objects) {
            super(context, 0, objects);
        }

        public void setActiveAccount(ActiveAccount activeAccount) {
            this.activeAccount = activeAccount;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Account account = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.account_item, parent, false);
            }

            if (account != null) {
                TextView textView = (TextView) convertView.findViewById(R.id.name);
                StringBuilder sr = new StringBuilder(account.name);

                if (activeAccount != null && activeAccount.isAccount(account)) {
                    sr.append(" ").append(getString(R.string.current));
                }
                textView.setText(sr.toString());
            }

            return convertView;
        }
    }
}
