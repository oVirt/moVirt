package org.ovirt.mobile.movirt.sync;

import android.content.ContentResolver;
import android.os.Bundle;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.provider.OVirtContract;

@EBean
public class SyncUtils {

    @Bean
    MovirtAuthenticator authenticator;

    /**
     * Helper method to trigger an immediate sync ("refresh").
     * <p>
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     * <p>
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public void triggerRefresh() {
//        Bundle b = new Bundle();
//        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
//        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
//        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
//        ContentResolver.requestSync(
//                authenticator.getActiveAccount(),      // Sync account
//                OVirtContract.CONTENT_AUTHORITY, // Content authority
//                b);                                      // Extras
    }
}
