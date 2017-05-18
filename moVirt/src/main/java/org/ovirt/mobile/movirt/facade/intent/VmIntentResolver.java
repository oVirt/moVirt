package org.ovirt.mobile.movirt.facade.intent;

import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.ui.vms.VmDetailActivity_;

@EBean(scope = EBean.Scope.Singleton)
public class VmIntentResolver extends BaseEntityIntentResolver<Vm> {

    @Override
    public Intent getDetailIntent(Vm entity, Context context, MovirtAccount account) {
        if (account == null || entity == null || context == null) {
            return null;
        }

        Intent intent = new Intent(context, VmDetailActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(entity.getUri());
        intent.putExtra(Constants.ACCOUNT_KEY, account);

        return intent;
    }
}
