package org.ovirt.mobile.movirt.ui.listfragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.AccountEnvironment;
import org.ovirt.mobile.movirt.model.base.OVirtAccountEntity;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class MultipleFacadeBaseListFragment<E extends OVirtAccountEntity> extends AccountFacadeBaseListFragment<E> {

    protected MultipleFacadeBaseListFragment(Class<E> clazz) {
        super(clazz);
    }

    @Override
    protected void appendQuery(ProviderFacade.QueryBuilder<E> query) {
        super.appendQuery(query);

        if (isMultiple() && !activeSelection.isAllAccounts()) {
            query.where(OVirtAccountEntity.ACCOUNT_ID, activeSelection.getAccount().getId());
        }
    }

    @Override
    @Background
    public void onRefresh() {
        if (isSingle()) {
            super.onRefresh();
        } else {
            try {
                Collection<AccountEnvironment> environments;

                if (activeSelection.isAllAccounts()) {
                    environments = environmentStore.getAllEnvironments();
                } else {
                    environments = Collections.singletonList(environmentStore.getEnvironment(activeSelection.getAccount()));
                }

                List<AccountEnvironment> filtered = new ArrayList<>();
                for (AccountEnvironment env : environments) {
                    if (!env.getAccountPropertiesManager().isFirstLogin()) {
                        filtered.add(env);
                    }
                }

                for (int i = 0; i < filtered.size(); i++) {
                    if (i == filtered.size() - 1) {
                        filtered.get(i).getFacade(entityClass).syncAll(new ProgressBarResponse<>(this));
                    } else {
                        filtered.get(i).getFacade(entityClass).syncAll();
                    }
                }
            } catch (AccountDeletedException ignore) {
            }
        }
    }
}

