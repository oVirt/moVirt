package org.ovirt.mobile.movirt.model;

import android.content.ContentProviderClient;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import org.ovirt.mobile.movirt.provider.OVirtContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TriggerResolver<E extends OVirtEntity> {

    private static final String TAG = TriggerResolver.class.getSimpleName();

    public abstract List<Trigger<E>> getTriggersForId(ContentProviderClient client, String id);

    private static final Map<Class<?>, TriggerResolver<?>> resolvers = new HashMap<>();

    private static final TriggerResolver<Vm> VM_RESOLVER = new TriggerResolver<Vm>() {
        @Override
        public List<Trigger<Vm>> getTriggersForId(ContentProviderClient client, String id) {
            final ArrayList<Trigger<Vm>> triggers = new ArrayList<>();
            try {
                triggers.addAll(getVmTriggers(client, id));
                triggers.addAll(getClusterTriggers(client, id));
                triggers.addAll(getGlobalTriggers(client));
                return triggers;
            } catch (RemoteException e) {
                Log.e(TAG, "Error resolving triggers for vm: " + id);
                return Collections.emptyList();
            }
        }

        private List<Trigger<Vm>> getVmTriggers(ContentProviderClient client, String vmId) throws RemoteException {
            Cursor cursor = client.query(OVirtContract.Trigger.CONTENT_URI,
                                         null,
                                         OVirtContract.Trigger.TARGET_ID + " = ?",
                                         new String[] {vmId},
                                         null);
            return collectCursorData(cursor);
        }

        private List<Trigger<Vm>> getClusterTriggers(ContentProviderClient client, String vmId) throws RemoteException {
            Cursor vmCursor = client.query(OVirtContract.Vm.CONTENT_URI.buildUpon().appendPath(vmId).build(), null, null, null, null, null);
            vmCursor.moveToNext();
            Vm vm = EntityMapper.VM_MAPPER.fromCursor(vmCursor);
            Cursor cursor = client.query(OVirtContract.Trigger.CONTENT_URI, null, OVirtContract.Trigger.TARGET_ID + " = ?", new String[] {vm.getClusterId()}, null);
            return collectCursorData(cursor);
        }

        private List<Trigger<Vm>> getGlobalTriggers(ContentProviderClient client) throws RemoteException {
            Cursor cursor = client.query(OVirtContract.Trigger.CONTENT_URI,
                                         null,
                                         OVirtContract.Trigger.SCOPE + " = ?",
                                         new String[] {Trigger.Scope.GLOBAL.toString()},
                                         null);
            return collectCursorData(cursor);
        }

        private List<Trigger<Vm>> collectCursorData(Cursor cursor) {
            final List<Trigger<Vm>> result = new ArrayList<>();
            while (cursor.moveToNext()) {
                result.add((Trigger<Vm>) EntityMapper.TRIGGER_MAPPER.fromCursor(cursor));
            }
            return result;
        }
    };

    static {
        resolvers.put(Vm.class, VM_RESOLVER);
    }

    @SuppressWarnings("unchecked")
    public static <E extends OVirtEntity> TriggerResolver<E> forEntity(Class<E> clazz) {
        return (TriggerResolver<E>) resolvers.get(clazz);
    }
}
