package org.ovirt.mobile.movirt.provider;

import android.content.ContentProviderClient;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.model.BaseEntity;

@EBean
public class ProviderFacade {
    public static final String TAG = ProviderFacade.class.getSimpleName();

    @RootContext
    Context context;

    private ContentProviderClient contentClient;

    @AfterInject
    void initContentProviderClient() {
        contentClient = context.getContentResolver().acquireContentProviderClient(OVirtContract.BASE_CONTENT_URI);
    }

    public <E extends BaseEntity<?>> void insert(E entity) {
        try {
            contentClient.insert(entity.getBaseUri(), entity.toValues());
        } catch (RemoteException e) {
            Log.e(TAG, "Error inserting entity: " + entity.toString(), e);
        }
    }

    public <E extends BaseEntity<?>> void update(E entity) {
        try {
            contentClient.update(entity.getUri(), entity.toValues(), null, null);
        } catch (RemoteException e) {
            Log.e(TAG, "Error updating entity: " + entity.toString(), e);
        }
    }

    public <E extends BaseEntity<?>> void delete(E entity) {
        try {
            contentClient.delete(entity.getUri(), null, null);
        } catch (RemoteException e) {
            Log.e(TAG, "Error deleting entity: " + entity.toString(), e);
        }
    }
}
