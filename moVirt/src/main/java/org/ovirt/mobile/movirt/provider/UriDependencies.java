package org.ovirt.mobile.movirt.provider;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean(scope = EBean.Scope.Singleton)
public class UriDependencies {

    @RootContext
    Context context;

    @AfterInject
    void init() {
        for (final UriDependency dependency : UriMatcher.getUriDependencies()) {
            context.getContentResolver().registerContentObserver(dependency.observe, true, new ContentObserver(null) {
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    context.getContentResolver().notifyChange(dependency.notify, null);
                }
            });
        }
    }

    static class UriDependency {
        private Uri observe;
        private Uri notify;

        UriDependency(Uri observe, Uri notify) {
            this.observe = observe;
            this.notify = notify;
        }
    }
}
