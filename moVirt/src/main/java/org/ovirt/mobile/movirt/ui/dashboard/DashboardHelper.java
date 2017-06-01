package org.ovirt.mobile.movirt.ui.dashboard;

import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

public class DashboardHelper {
    public static <E extends OVirtEntity> ProviderFacade.QueryBuilder<E> querySelection(ProviderFacade providerFacade, Class<E> clazz, ActiveSelection selection) {
        final ProviderFacade.QueryBuilder<E> queryBuilder = providerFacade.query(clazz);

        if (!selection.isAllAccounts()) {
            queryBuilder.where(OVirtContract.AccountEntity.ACCOUNT_ID, selection.getAccountId());
        }

        if (OVirtContract.HasCluster.class.isAssignableFrom(clazz) && selection.isCluster()) {
            queryBuilder.where(OVirtContract.HasCluster.CLUSTER_ID, selection.getClusterId());
        }

        return queryBuilder;
    }
}
