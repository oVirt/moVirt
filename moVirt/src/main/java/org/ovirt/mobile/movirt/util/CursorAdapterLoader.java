package org.ovirt.mobile.movirt.util;

import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.CursorAdapter;

public abstract class CursorAdapterLoader implements LoaderManager.LoaderCallbacks<Cursor> {
    private final CursorAdapter cursorAdapter;

    public CursorAdapterLoader(CursorAdapter cursorAdapter) {
        this.cursorAdapter = cursorAdapter;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}
