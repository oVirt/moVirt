package org.ovirt.mobile.movirt.provider;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.model.base.BaseEntity;
import org.ovirt.mobile.movirt.model.mapping.EntityMapper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    public class QueryBuilder<E extends BaseEntity<?>> {
        private static final String URI_FIELD_NAME = "CONTENT_URI";

        private final Class<E> clazz;
        private final Uri baseUri;

        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();
        StringBuilder sortOrder = new StringBuilder();
        String limitClause = "";

        String[] projection;

        public QueryBuilder(Class<E> clazz) {
            this.clazz = clazz;
            try {
                this.baseUri = (Uri) clazz.getField(URI_FIELD_NAME).get(null);
            } catch (Exception e) { // NoSuchFieldException | IllegalAccessException -  since SDK version 19
                throw new RuntimeException("Assertion error: Class: " + clazz + " does not define static field " + URI_FIELD_NAME, e);
            }
        }

        public QueryBuilder<E> projection(String[] projection) {
            this.projection = projection;
            return this;
        }

        public QueryBuilder<E> id(String value) {
            return where(OVirtContract.BaseEntity.ID, value);
        }

        public QueryBuilder<E> whereLike(String columnName, String value) {
            return where(columnName, value, Relation.IS_LIKE);
        }

        public QueryBuilder<E> where(String columnName, String value) {
            return where(columnName, value, Relation.IS_EQUAL);
        }

        public QueryBuilder<E> whereIn(String columnName, String[] values) {
            assertNotEmpty(columnName);

            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append(columnName);
            if (values == null || values.length == 0) {
                selection.append(" IS NULL ");
            } else {
                selection.append(Relation.IN.getVal()).append(" (");
                for (int i = 0; i < values.length; i++) {
                    selection.append("? ");
                    if (i == values.length - 1) {
                        selection.append(") ");
                    } else {
                        selection.append(", ");
                    }
                }
                selectionArgs.addAll(Arrays.asList(values));
            }

            return this;
        }

        public QueryBuilder<E> empty(String columnName) {
            assertNotEmpty(columnName);

            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append('(').append(columnName).append(" IS NULL OR ")
                    .append(columnName).append(Relation.IS_EQUAL.getVal()).append("'') ");

            return this;
        }

        public QueryBuilder<E> whereNotEqual(String columnName, String value) {
            return where(columnName, value, Relation.NOT_EQUAL);
        }

        public QueryBuilder<E> where(String columnName, String value, Relation relation) {
            assertNotEmpty(columnName);

            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append(columnName);
            if (value == null) {
                selection.append(" IS NULL ");
            } else {
                selection.append(relation.getVal()).append("? ");
                selectionArgs.add(value);
            }

            return this;
        }

        public QueryBuilder<E> orderBy(String columnName) {
            return orderBy(columnName, SortOrder.ASCENDING);
        }

        public QueryBuilder<E> orderByAscending(String columnName) {
            return orderBy(columnName);
        }

        public QueryBuilder<E> orderByDescending(String columnName) {
            return orderBy(columnName, SortOrder.DESCENDING);
        }

        public QueryBuilder<E> limit(int limit) {
            limitClause = " LIMIT " + Integer.toString(limit);
            return this;
        }

        private String sortOrderWithLimit() {
            StringBuilder res = new StringBuilder();
            String sortOrderString = sortOrder.toString();
            res.append(!"".equals(sortOrderString) ? sortOrderString : OVirtContract.ROW_ID);
            res.append(limitClause);

            return res.toString();
        }

        public QueryBuilder<E> orderBy(String columnName, SortOrder order) {
            assertNotEmpty(columnName);

            if (sortOrder.length() > 0) {
                sortOrder.append(", ");
            }
            sortOrder.append(columnName).append(" ");
            sortOrder.append(order).append(" ");

            return this;
        }

        public Cursor asCursor() {
            try {
                return contentClient.query(baseUri,
                        projection,
                        selection.toString(),
                        selectionArgs.toArray(new String[selectionArgs.size()]),
                        sortOrderWithLimit());
            } catch (RemoteException e) {
                Log.e(TAG, "Error querying " + baseUri, e);
                throw new RuntimeException(e);
            }
        }

        public Loader<Cursor> asLoader() {
            return new CursorLoader(context,
                    baseUri,
                    projection,
                    selection.toString(),
                    selectionArgs.toArray(new String[selectionArgs.size()]),
                    sortOrderWithLimit());
        }

        public Collection<E> all() {
            Cursor cursor = asCursor();
            if (cursor == null) {
                return Collections.emptyList();
            }

            try {
                List<E> result = new ArrayList<>();
                while (cursor.moveToNext()) {
                    result.add(EntityMapper.forEntity(clazz).fromCursor(cursor));
                }
                return result;
            } finally {
                ObjectUtils.closeSilently(cursor);
            }
        }

        public E first() {
            Cursor cursor = asCursor();
            if (cursor == null) {
                return null;
            }
            try {
                return cursor.moveToNext() ? EntityMapper.forEntity(clazz).fromCursor(cursor) : null;
            } finally {
                ObjectUtils.closeSilently(cursor);
            }
        }
    }

    public class BatchBuilder {
        private ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        public <E extends BaseEntity<?>> BatchBuilder insert(E entity) {
            batch.add(ContentProviderOperation.newInsert(entity.getBaseUri()).withValues(entity.toValues()).build());
            return this;
        }

        public <E extends BaseEntity<?>> BatchBuilder update(E entity) {
            batch.add(ContentProviderOperation.newUpdate(entity.getUri()).withValues(entity.toValues()).build());
            return this;
        }

        public <E extends BaseEntity<?>> BatchBuilder delete(E entity) {
            batch.add(ContentProviderOperation.newDelete(entity.getUri()).build());
            return this;
        }

        public void apply() {
            try {
                contentClient.applyBatch(batch);
            } catch (RemoteException | OperationApplicationException e) {
                throw new RuntimeException("Batch apply failed", e);
            }
        }

        public int size() {
            return batch.size();
        }

        public boolean isEmpty() {
            return batch.isEmpty();
        }
    }

    public <E extends BaseEntity<?>> QueryBuilder<E> query(Class<E> clazz) {
        return new QueryBuilder<>(clazz);
    }

    public <E extends BaseEntity<?>> void insert(E entity) {
        try {
            contentClient.insert(entity.getBaseUri(), entity.toValues());
        } catch (RemoteException e) {
            Log.e(TAG, "Error inserting entity: " + entity, e);
        }
    }

    public <E extends BaseEntity<?>> void update(E entity) {
        try {
            contentClient.update(entity.getUri(), entity.toValues(), null, null);
        } catch (RemoteException e) {
            Log.e(TAG, "Error updating entity: " + entity, e);
        }
    }

    public <E extends BaseEntity<?>> void delete(E entity) {
        deleteAll(entity.getUri());
    }

    public int deleteAll(Uri uri) {
        try {
            return contentClient.delete(uri, null, null);
        } catch (RemoteException e) {
            Log.e(TAG, "Error deleting entities with uri: " + uri, e);
        }

        return -1;
    }

    public BatchBuilder batch() {
        return new BatchBuilder();
    }

    private void assertNotEmpty(String columnName) {
        if (TextUtils.isEmpty(columnName)) {
            throw new IllegalArgumentException("columnName cannot be empty or null");
        }
    }
}
