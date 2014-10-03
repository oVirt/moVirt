package org.ovirt.mobile.movirt.rest;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.api.rest.RestErrorHandler;
import org.springframework.web.client.RestClientException;
import org.ovirt.mobile.movirt.R;

@EBean
public class ErrorHandler implements RestErrorHandler {

    private static final String TAG = ErrorHandler.class.getSimpleName();

    @StringRes(R.string.rest_request_failed)
    String errorMsg;

    @RootContext
    Context context;

    @Override
    public void onRestClientExceptionThrown(RestClientException e) {
        Log.e(TAG, "Error during calling REST: '" + e.getMessage() + "'");

        final String msg = String.format(errorMsg, e.getMessage());
        makeToast(msg);
    }

    @UiThread
    void makeToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}
