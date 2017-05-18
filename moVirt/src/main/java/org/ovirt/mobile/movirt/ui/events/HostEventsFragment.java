package org.ovirt.mobile.movirt.ui.events;

import android.text.TextUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.Response;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Event.HOST_ID;

@EFragment(R.layout.fragment_base_entity_list)
public class HostEventsFragment extends NamedEntityResumeSyncableEventsFragment {

    @InstanceState
    String hostId;

    @AfterViews
    protected void init2() {
        if (hostId == null) {
            throw new IllegalArgumentException("host is null");
        }
    }

    @Override
    protected void appendQuery(ProviderFacade.QueryBuilder<Event> query) {
        super.appendQuery(query);
        query.where(HOST_ID, hostId);
    }

    public HostEventsFragment setHostId(String hostId) {
        this.hostId = hostId;
        return this;
    }

    @Override
    protected void sync(Response<List<Event>> response) {
        String hostName = getEntityName(Host.class, hostId);
        if (!TextUtils.isEmpty(hostName)) {
            try {
                environmentStore.getEnvironment(account).getHostEventFacade().syncAll(response, hostId, hostName);
            } catch (AccountDeletedException ignore) {
            }
        }
    }
}
