package org.ovirt.mobile.movirt.sync;

import android.util.Log;

import org.androidannotations.annotations.EBean;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by sphoorti on 18/11/14.
 */
@EBean
public class NullHostnameVerifier implements HostnameVerifier {
    private static final String TAG = NullHostnameVerifier.class.getSimpleName();

    public boolean verify(String hostname, SSLSession session) {
        Log.d(TAG,"Inside Verify");
        return true;
    }
}
