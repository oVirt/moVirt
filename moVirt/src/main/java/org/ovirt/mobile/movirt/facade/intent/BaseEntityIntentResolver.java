package org.ovirt.mobile.movirt.facade.intent;

import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.base.OVirtAccountEntity;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;

@EBean
public abstract class BaseEntityIntentResolver<E extends OVirtEntity> implements EntityIntentResolver<E> {

    @Bean
    AccountRxStore rxStore;

    public abstract Intent getDetailIntent(E entity, Context context, MovirtAccount account);

    public Intent getDetailIntent(E entity, Context context) {
        MovirtAccount account = null;
        if (OVirtAccountEntity.class.isAssignableFrom(entity.getClass())) {
            final String accountId = ((OVirtAccountEntity) entity).getAccountId();
            account = rxStore.getAllAccountsWrapped().getAccountById(accountId);
        }

        return getDetailIntent(entity, context, account);
    }

    @Override
    public boolean hasIntent(E entity) {
        return OVirtAccountEntity.class.isAssignableFrom(entity.getClass());
    }
}
