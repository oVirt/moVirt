package org.ovirt.mobile.movirt.rest;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.EBean;
import org.androidannotations.api.rest.RestErrorHandler;
import org.springframework.web.client.RestClientException;
import org.ovirt.mobile.movirt.R;

@EBean
public class ErrorHandler implements RestErrorHandler {

    private static final String TAG = ErrorHandler.class.getSimpleName();

    private Activity context;

    @Override
    public void onRestClientExceptionThrown(RestClientException e) {
        Log.e(TAG, "Error during calling REST: '" + e.getMessage() + "'");

        if (context == null) {
            return;
        }

        final String msg = context.getResources().getString(R.string.rest_request_failed, e.getMessage());

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setContext(Activity context) {
        this.context = context;
    }
}
