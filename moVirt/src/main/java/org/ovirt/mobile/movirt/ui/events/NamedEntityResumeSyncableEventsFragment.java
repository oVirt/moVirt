package org.ovirt.mobile.movirt.ui.events;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.Receiver;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.base.OVirtNamedEntity;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;

import java.util.List;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class NamedEntityResumeSyncableEventsFragment extends EventsFragment {

    @InstanceState
    protected boolean resumeSynced = false;

    private boolean lastSyncBroadcast;

    private String entityName;

    @Override
    public void onResume() {
        super.onResume();
        if (!resumeSynced) {
            resumeSynced = true;
            onRefresh();
        }
    }

    protected <E extends OVirtNamedEntity> String getEntityName(Class<E> clazz, String id) {
        if (entityName == null) {
            synchronized (this) {
                if (entityName == null) {
                    E entity = provider.query(clazz).id(id).first();
                    if (entity != null) {
                        entityName = entity.getName();
                    }
                }
            }
        }

        return entityName;
    }

    @Background
    @Receiver(actions = Broadcasts.IN_SYNC, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void syncingChanged(@Receiver.Extra(Broadcasts.Extras.SYNCING) boolean syncing) {
        if (lastSyncBroadcast && !syncing) { // sync fragment after main sync
            sync(null);
        }
        lastSyncBroadcast = syncing;
    }

    @Background
    @Override
    public void onRefresh() {
        sync(new ProgressBarResponse<List<Event>>(this));
    }

    protected abstract void sync(Response<List<Event>> response);
}
