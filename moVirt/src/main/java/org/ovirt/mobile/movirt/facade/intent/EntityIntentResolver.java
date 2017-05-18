package org.ovirt.mobile.movirt.facade.intent;

import android.content.Context;
import android.content.Intent;

import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;

public interface EntityIntentResolver<E extends OVirtEntity> {

    Intent getDetailIntent(E entity, Context context, MovirtAccount account);

    Intent getDetailIntent(E entity, Context context);

    boolean hasIntent(E entity);
}
