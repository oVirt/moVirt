package org.ovirt.mobile.movirt.ui.account;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

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
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.ui.PresenterBroadcastAwareActivity;
import org.ovirt.mobile.movirt.ui.SettingsActivity_;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;
import org.ovirt.mobile.movirt.util.message.MessageHelper;

import java.util.List;

@EActivity(R.layout.activity_edit_accounts)
@OptionsMenu(R.menu.delete_item)
public class EditAccountsActivity extends PresenterBroadcastAwareActivity
        implements ConfirmDialogFragment.ConfirmDialogListener, EditAccountsContract.View {

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
    MessageHelper messageHelper;

    @InstanceState
    Integer selectedListItem;

    private EditAccountsContract.Presenter presenter;

    @AfterViews
    void init() {
        setTitle(TITLE);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_green_300)));
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        presenter = EditAccountsPresenter_.getInstance_(getApplicationContext())
                .setView(this)
                .initialize();

        fab.setOnClickListener(view -> presenter.addAccount());
    }

    @Override
    public BasePresenter getPresenter() {
        return presenter;
    }

    @Override
    public void showAccounts(List<AccountWrapper> accounts, ActiveSelection activeSelection) {
        final AccountsAdapter adapter = new AccountsAdapter(this, getApplicationContext(),
                accounts.toArray(new AccountWrapper[accounts.size()]));

        adapter.setActiveAccount(activeSelection);

        listView.setAdapter(adapter);

        if (selectedListItem != null) {
            listViewItemLongClicked(selectedListItem);
        }
    }

    @Override
    public void startAddAccountActivity() {
        Intent intent = new Intent(getApplicationContext(), AddAccountActivity_.class);
        startActivity(intent);
    }

    @Override
    public void startSettingsAccountActivity(MovirtAccount account) {
        Intent intent = new Intent(this, SettingsActivity_.class);
        intent.putExtra(Constants.ACCOUNT_KEY, account);
        startActivity(intent);
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
        presenter.accountClicked(wrapper.account);
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
            presenter.deleteAccount(account);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        deleteItem.setVisible(isSelected());
        return super.onPrepareOptionsMenu(menu);
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
        if (listView.getCount() <= 0) {
            return null;
        }

        int position = listView.getCheckedItemPosition();
        AccountWrapper wrapper = (AccountWrapper) listView.getItemAtPosition(position);
        return wrapper == null ? null : wrapper.account;
    }
}
