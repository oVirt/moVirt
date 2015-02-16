package org.ovirt.mobile.movirt.rest;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.api.rest.RestErrorHandler;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.springframework.core.NestedRuntimeException;

@EBean
public class ErrorHandler implements RestErrorHandler {

    private static final String TAG = ErrorHandler.class.getSimpleName();

    @StringRes(R.string.rest_request_failed)
    String errorMsg;

    @RootContext
    Context context;

    @Override
    public void onRestClientExceptionThrown(NestedRuntimeException e) {
        Log.e(TAG, "Error during calling REST: '" + e.getMessage() + "'");

        final String msg = String.format(errorMsg, e.getMessage());

        Intent intent = new Intent(Broadcasts.CONNECTION_FAILURE);
        intent.putExtra(Broadcasts.Extras.CONNECTION_FAILURE_REASON, msg);
        context.sendBroadcast(intent);
    }

}
