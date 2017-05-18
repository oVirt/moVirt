package org.ovirt.mobile.movirt.facade.intent;

import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.enums.SnapshotType;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.ui.snapshots.SnapshotDetailActivity_;

@EBean(scope = EBean.Scope.Singleton)
public class SnapshotIntentResolver extends BaseEntityIntentResolver<Snapshot> {

    @Override
    public Intent getDetailIntent(Snapshot entity, Context context, MovirtAccount account) {
        if (account == null || entity == null || context == null) {
            return null;
        }

        Intent intent = new Intent(context, SnapshotDetailActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(entity.getUri());
        intent.putExtra(OVirtContract.HasVm.VM_ID, entity.getVmId());
        intent.putExtra(Constants.ACCOUNT_KEY, account);

        return intent;
    }

    @Override
    public boolean hasIntent(Snapshot entity) {
        return super.hasIntent(entity) && entity.getType() != SnapshotType.ACTIVE;
    }
}
