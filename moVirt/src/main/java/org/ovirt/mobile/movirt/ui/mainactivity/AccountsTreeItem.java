package org.ovirt.mobile.movirt.ui.mainactivity;

import android.content.Context;

import com.unnamed.b.atv.model.TreeNode;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.ui.TreeHolder;

public class AccountsTreeItem implements TreeHolder.HolderNode {
    private MovirtAccount account;
    private Cluster cluster;

    private ActiveSelectionChangedListener changedListener;
    private LongClickListener longClickListener;

    public AccountsTreeItem(MovirtAccount account, Cluster cluster, ActiveSelectionChangedListener changedListener, LongClickListener longClickListener) {
        this.account = account;
        this.cluster = cluster;
        this.changedListener = changedListener;
        this.longClickListener = longClickListener;
    }

    @Override
    public void onSelect() {
        if (changedListener != null) {
            changedListener.onSelect(asActiveSelection());
        }
    }

    @Override
    public void onLongClick() {
        if (longClickListener != null) {
            longClickListener.onLongClick(asActiveSelection());
        }
    }

    public ActiveSelection asActiveSelection() {
        if (account == null) {
            return ActiveSelection.ALL_ACTIVE;
        } else if (cluster == null) {
            return new ActiveSelection(account);
        } else {
            return new ActiveSelection(account, cluster.getId(), cluster.getName());
        }
    }

    @Override
    public String getDescription(Context context, TreeNode treeNode) {
        if (cluster != null) {
            return cluster.getName();
        }

        if (account != null) {
            return account.getName();
        }

        return context.getString(R.string.all);
    }

    public interface ActiveSelectionChangedListener {
        void onSelect(ActiveSelection activeSelection);
    }

    public interface LongClickListener {
        void onLongClick(ActiveSelection activeSelection);
    }
}
