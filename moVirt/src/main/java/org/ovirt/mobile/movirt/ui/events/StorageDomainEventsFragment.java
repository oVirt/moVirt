package org.ovirt.mobile.movirt.ui.events;

import android.text.TextUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.Response;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Event.STORAGE_DOMAIN_ID;

@EFragment(R.layout.fragment_base_entity_list)
public class StorageDomainEventsFragment extends NamedEntityResumeSyncableEventsFragment {

    @InstanceState
    String storageDomainId;

    @AfterViews
    protected void init2() {
        if (storageDomainId == null) {
            throw new IllegalArgumentException("storageDomainId is null");
        }
    }

    @Override
    protected void appendQuery(ProviderFacade.QueryBuilder<Event> query) {
        super.appendQuery(query);
        query.where(STORAGE_DOMAIN_ID, storageDomainId);
    }

    public StorageDomainEventsFragment setStorageDomainId(String storageDomainId) {
        this.storageDomainId = storageDomainId;
        return this;
    }

    @Override
    protected void sync(Response<List<Event>> response) {
        String storageDomainName = getEntityName(StorageDomain.class, storageDomainId);
        if (!TextUtils.isEmpty(storageDomainName)) {
            try {
                environmentStore.getEnvironment(account).getStorageDomainEventFacade().syncAll(response, storageDomainId, storageDomainName);
            } catch (AccountDeletedException ignore) {
            }
        }
    }
}
