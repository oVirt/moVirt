package org.ovirt.mobile.movirt.ui.triggers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import org.ovirt.mobile.movirt.auth.account.data.Selection;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.PresenterStatusSyncableActivity;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;

import java.util.List;

@EActivity(R.layout.activity_edit_triggers)
@OptionsMenu(R.menu.delete_item)
public class EditTriggersActivity extends PresenterStatusSyncableActivity implements EditTriggersContract.View, ConfirmDialogFragment.ConfirmDialogListener {
    public static final String EXTRA_TARGET_ENTITY_ID = "target_entity";
    public static final String EXTRA_SELECTION = "selection";

    private static final String[] PROJECTION = new String[]{
            OVirtContract.Trigger.CONDITION,
            OVirtContract.Trigger.NOTIFICATION,
    };
    private static final int DELETE_ACTION = 0;

    @Bean
    ProviderFacade provider;

    @ViewById
    FloatingActionButton fab;

    @StringRes(R.string.whole_datacenter)
    String GLOBAL_SCOPE;

    @StringRes(R.string.cluster_scope)
    String CLUSTER_SCOPE;

    @StringRes(R.string.vm_scope)
    String ITEM_SCOPE;

    @StringRes(R.string.trigger_title_format)
    String TITLE_FORMAT;

    @OptionsMenuItem
    MenuItem deleteItem;

    @ViewById
    ListView listView;

    @InstanceState
    Integer selectedListItem;

    @ViewById
    ProgressBar progress;

    private EditTriggersContract.Presenter presenter;

    @AfterViews
    void init() {
        setProgressBar(progress);

        presenter = EditTriggersPresenter_.getInstance_(getApplicationContext())
                .setEntityId(getIntent().getStringExtra(EXTRA_TARGET_ENTITY_ID))
                .setSelection(getIntent().getParcelableExtra(EXTRA_SELECTION))
                .setView(this)
                .initialize();

        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setEmptyView(findViewById(android.R.id.empty));

        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_green_300)));
        fab.setOnClickListener(view -> presenter.addTrigger());
    }

    @Override
    public BasePresenter getPresenter() {
        return presenter;
    }

    @Override
    public void showTriggers(List<ViewTrigger> triggers) {
        final TriggersAdapter adapter = new TriggersAdapter(getApplicationContext(), triggers.toArray(new ViewTrigger[triggers.size()]));

        listView.setAdapter(adapter);

        if (selectedListItem != null) {
            listViewItemLongClicked(selectedListItem);
        }
    }

    @Override
    public void startEditTriggerActivity(ViewTrigger trigger) {
        Intent intent = getTriggerActivityIntent(EditTriggerActivity_.class, trigger.trigger.getUri());
        intent.putExtra(EXTRA_SELECTION, trigger.selection);
        startActivity(intent);
    }

    @Override
    public void startAddTriggerActivity(Selection selection) {
        clearSelection();
        Intent intent = getTriggerActivityIntent(AddTriggerActivity_.class);
        intent.putExtra(EXTRA_SELECTION, selection);
        startActivity(intent);
    }

    @ItemLongClick(R.id.listView)
    void listViewItemLongClicked(int position) {
        selectedListItem = position;
        listView.setItemChecked(position, true);
        updateSelection();
    }

    @ItemClick(R.id.listView)
    void listViewItemClicked(ViewTrigger trigger) {
        clearSelection();
        presenter.triggerClicked(trigger);
    }

    @OptionsItem(R.id.delete_item)
    void delete() {
        ConfirmDialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(DELETE_ACTION, getString(R.string.dialog_action_delete_trigger));
        confirmDialog.show(getFragmentManager(), "confirmDeleteTrigger");
    }

    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (actionId == DELETE_ACTION && dialogButton == DialogInterface.BUTTON_POSITIVE) {
            ViewTrigger trigger = getSelectedListItem();
            clearSelection();
            presenter.deleteTrigger(trigger);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        deleteItem.setVisible(isSelected());
        return super.onPrepareOptionsMenu(menu);
    }

    private Intent getTriggerActivityIntent(Class<?> clazz) {
        return getTriggerActivityIntent(clazz, null);
    }

    private Intent getTriggerActivityIntent(Class<?> clazz, Uri uri) {
        Intent intent = new Intent(getApplicationContext(), clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtras(getIntent());
        if (uri != null) {
            intent.setData(uri);
        }

        return intent;
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        if (isSelected()) {
            clearSelection();
        } else {
            onBackPressed(); // home behaves like back button - we need to return to vm from triggers
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
        presenter.triggerSelected(getSelectedListItem());
        invalidateOptionsMenu(); // delete button
    }

    private boolean isSelected() {
        return listView.getCheckedItemPosition() >= 0;
    }

    private ViewTrigger getSelectedListItem() {
        int position = listView.getCheckedItemPosition();
        ViewTrigger trigger = null;

        if (position >= 0 && listView.getCount() > 0) {
            trigger = (ViewTrigger) listView.getItemAtPosition(position);
        }

        return trigger;
    }

    private class TriggersAdapter extends ArrayAdapter<ViewTrigger> {

        public TriggersAdapter(Context context, ViewTrigger[] objects) {
            super(context, R.layout.trigger_item, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewTrigger triggerWrapper = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.trigger_item, parent, false);
            }

            if (triggerWrapper != null && triggerWrapper.trigger != null) {
                Trigger trigger = triggerWrapper.trigger;

                TextView condition = (TextView) convertView.findViewById(R.id.trigger_condition);

                condition.setText(trigger.toString());

                TextView notification = (TextView) convertView.findViewById(R.id.trigger_notification);
                notification.setText(trigger.getNotificationType().getDisplayResourceId());

                TextView path = (TextView) convertView.findViewById(R.id.trigger_path);

                final int dimmedColor = getResources().getColor(triggerWrapper.highlight ? R.color.abc_primary_text_material_dark : R.color.material_grey_400);
                path.setTextColor(dimmedColor);

                SpannableString spanString = new SpannableString(triggerWrapper.getPath());
                spanString.setSpan(new StyleSpan(triggerWrapper.highlight ? Typeface.BOLD : Typeface.NORMAL), 0, spanString.length(), 0);
                path.setText(spanString);
            }

            return convertView;
        }
    }
}
