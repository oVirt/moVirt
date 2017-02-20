package org.ovirt.mobile.movirt.provider;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.os.RemoteException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.ovirt.mobile.movirt.util.message.MessageHelper;

@EBean
public class EventProviderHelper {
    public static final String TAG = EventProviderHelper.class.getSimpleName();

    @RootContext
    Context context;

    @Bean
    MessageHelper messageHelper;

    private ContentProviderClient contentClient;

    @AfterInject
    void initContentProviderClient() {
        contentClient = context.getContentResolver().acquireContentProviderClient(OVirtContract.BASE_CONTENT_URI);
    }

    public int deleteEvents() throws EventProviderException {
        try {
            return contentClient.delete(OVirtContract.Event.CONTENT_URI, null, null);
        } catch (Exception e) {
            throw new EventProviderException("Error deleting all events", e);
        }
    }

    public void deleteEventsAndLetOnly(int leave) throws EventProviderException {
        try {
            if (leave < 1) {
                deleteEvents();
                return;
            }

            int id = getSmallestFrom(leave);
            if (id != 0) {

                contentClient.delete(OVirtContract.Event.CONTENT_URI,
                        OVirtContract.Event.ID + " < ?",
                        new String[]{Integer.toString(id)}
                );
            }
        } catch (Exception e) {
            throw new EventProviderException("Error deleting old events", e);
        }
    }

    public void deleteTemporaryEvents() throws EventProviderException {
        try {
            contentClient.delete(OVirtContract.Event.CONTENT_URI,
                    OVirtContract.Event.TEMPORARY + " > 0", // true
                    null
            );
        } catch (Exception e) {
            throw new EventProviderException("Error deleting temporary events", e);
        }
    }

    private int getSmallestFrom(int from) throws EventProviderException {
        Cursor cursor = null;
        try {
            cursor = contentClient.query(OVirtContract.Event.CONTENT_URI,
                    new String[]{OVirtContract.Event.ID},
                    null,
                    null,
                    OVirtContract.Event.ID + " DESC LIMIT " + from);

            if (cursor != null && cursor.moveToLast()) {
                return cursor.getInt(0);
            }
        } catch (RemoteException e) {
            throw new EventProviderException("Error determining last event id", e);
        } finally {
            ObjectUtils.closeSilently(cursor);
        }
        return 0;
    }

    public int getLastEventId() throws EventProviderException {
        Cursor cursor = null;
        try {
            cursor = contentClient.query(OVirtContract.Event.CONTENT_URI,
                    new String[]{"MAX(" + OVirtContract.Event.ID + ")"},
                    null,
                    null,
                    null);
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        } catch (RemoteException e) {
            throw new EventProviderException("Error determining last event id", e);
        } finally {
            ObjectUtils.closeSilently(cursor);
        }
        return 0;
    }

    public class EventProviderException extends Exception {
        EventProviderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
