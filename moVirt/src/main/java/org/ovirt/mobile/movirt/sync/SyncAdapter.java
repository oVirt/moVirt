package org.ovirt.mobile.movirt.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.BaseEntity;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.rest.OVirtClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EBean
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = SyncAdapter.class.getSimpleName();

    @Bean
    OVirtClient oVirtClient;

    ContentProviderClient contentClient;

    public SyncAdapter(Context context) {
        super(context, true);
        contentClient = context.getContentResolver().acquireContentProviderClient(OVirtContract.BASE_CONTENT_URI);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "Performing full sync for account[" + account.name + "]");

        try {
            final List<Vm> remoteVms = oVirtClient.getVms();
            final List<Cluster> remoteClusters = oVirtClient.getClusters();

            final ArrayList<ContentProviderOperation> batch = new ArrayList<>();
            batch.addAll(updateLocalEntities(OVirtContract.Cluster.CONTENT_URI, remoteClusters, new EntityMapper<Cluster>() {
                @Override
                public Cluster fromCursor(Cursor cursor) {
                    Cluster cluster = new Cluster();
                    cluster.setId(cursor.getString(cursor.getColumnIndex(OVirtContract.Cluster._ID)));
                    cluster.setName(cursor.getString(cursor.getColumnIndex(OVirtContract.Cluster.NAME)));
                    return cluster;
                }
            }));

            batch.addAll(updateLocalEntities(OVirtContract.Vm.CONTENT_URI, remoteVms, new EntityMapper<Vm>() {
                @Override
                public Vm fromCursor(Cursor cursor) {
                    Vm vm = new Vm();
                    vm.setId(cursor.getString(cursor.getColumnIndex(OVirtContract.Vm._ID)));
                    vm.setName(cursor.getString(cursor.getColumnIndex(OVirtContract.Vm.NAME)));
                    vm.setStatus(cursor.getString(cursor.getColumnIndex(OVirtContract.Vm.STATUS)));
                    vm.setClusterId(cursor.getString(cursor.getColumnIndex(OVirtContract.Vm.CLUSTER_ID)));
                    return vm;
                }
            }));

            if (batch.isEmpty()) {
                Log.i(TAG, "No updates necessary");
            } else {
                Log.i(TAG, "Applying batch update");
                contentClient.applyBatch(batch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <E extends BaseEntity> List<ContentProviderOperation> updateLocalEntities(Uri baseContentUri, List<E> remoteEntities, EntityMapper<E> builder)
            throws RemoteException {
        final ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        final Map<String, E> entityMap = groupEntitiesById(remoteEntities);

        final Cursor cursor = contentClient.query(baseContentUri, null, null, null, null);
        while (cursor.moveToNext()) {
            E localEntity = builder.fromCursor(cursor);
            E remoteEntity = entityMap.get(localEntity.getId());
            if (remoteEntity == null) { // local entity obsolete, schedule delete from db
                Uri deleteUri = baseContentUri.buildUpon().appendPath(localEntity.getId()).build();
                Log.i(TAG, "Scheduling delete for URI: " + deleteUri);
                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
            } else { // existing entity, update stats if changed
                entityMap.remove(localEntity.getId());
                if (!localEntity.equals(remoteEntity)) {
                    Uri existingUri = baseContentUri.buildUpon().appendPath(localEntity.getId()).build();
                    Log.i(TAG, "Scheduling update for URI: " + existingUri);
                    batch.add(ContentProviderOperation.newUpdate(existingUri).withValues(remoteEntity.toValues()).build());
                }
            }
        }

        for (E entity : entityMap.values()) {
            Log.i(TAG, "Scheduling insert for entity: id = " + entity.getId());
            batch.add(ContentProviderOperation.newInsert(baseContentUri).withValues(entity.toValues()).build());
        }

        return batch;
    }

    private static <E extends BaseEntity> Map<String, E> groupEntitiesById(List<E> entities) {
        Map<String, E> entityMap = new HashMap<>();
        for (E entity : entities) {
            entityMap.put(entity.getId(), entity);
        }
        return entityMap;
    }

    private static interface EntityMapper<E> {
        E fromCursor(Cursor cursor);
    }
}
