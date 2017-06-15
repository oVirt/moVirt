package org.ovirt.mobile.movirt.util.resources;

import android.content.Context;
import android.support.annotation.NonNull;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public abstract class StringResources {

    @RootContext
    public Context context;

    @NonNull
    public final String getString(@android.support.annotation.StringRes int resId) {
        return context.getString(resId);
    }

    @NonNull
    public final String getString(@android.support.annotation.StringRes int resId, Object... formatArgs) {
        return context.getString(resId, formatArgs);
    }
}
