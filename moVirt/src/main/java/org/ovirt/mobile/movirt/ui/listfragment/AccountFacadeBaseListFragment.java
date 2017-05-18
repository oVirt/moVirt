package org.ovirt.mobile.movirt.ui.listfragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.base.OVirtAccountEntity;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class AccountFacadeBaseListFragment<E extends OVirtAccountEntity> extends SearchBaseListFragment<E> {

    @InstanceState
    protected MovirtAccount account;

    protected AccountFacadeBaseListFragment(Class<E> clazz) {
        super(clazz);
    }

    public AccountFacadeBaseListFragment<E> setAccount(MovirtAccount account) {
        this.account = account;
        return this;
    }

    /**
     * Multiple Accounts (ActiveSelection)
     */
    protected boolean isMultiple() {
        return account == null;
    }

    /**
     * Single Account
     */
    protected boolean isSingle() {
        return account != null;
    }

    @Override
    protected void appendQuery(ProviderFacade.QueryBuilder<E> query) {
        super.appendQuery(query);
        if (isSingle()) {
            query.where(OVirtAccountEntity.ACCOUNT_ID, account.getId());
        }
    }

    @Override
    @Background
    public void onRefresh() {
        try {
            if (isSingle()) {
                environmentStore.getEnvironment(account).getFacade(entityClass).syncAll(new ProgressBarResponse<>(this));
            }
        } catch (AccountDeletedException ignore) {
        }
    }
}

